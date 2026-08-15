# Mini Database Engine

A relational database engine built from scratch in Java — disk-based storage, a B+ Tree index, and a SQL query interface. Built as a learning project to understand how real databases (PostgreSQL, MySQL, SQLite) work under the hood.

```
> INSERT INTO users VALUES(1, Divyansh)
Inserted 1 row.
> INSERT INTO users VALUES(2, Alice)
Inserted 1 row.
> SELECT * FROM users WHERE id = 1
[Record{id=1, name='Divyansh'}]
> DELETE FROM users WHERE id = 1
Deleted 1 row.
> SELECT * FROM users
[Record{id=2, name='Alice'}]
```

## Features

- **Disk-based storage** — data is organized into fixed-size 4KB pages, matching typical OS/disk block sizes, and persists across restarts
- **B+ Tree index** — O(log n) insert, search, and delete, with recursive multi-level node splitting and merging (borrow-from-sibling / merge-on-underflow)
- **SQL support** — a hand-written tokenizer and recursive-descent parser supporting `SELECT` (with and without `WHERE`), `INSERT`, and `DELETE`
- **Tombstone deletion** — deletes mark records rather than physically removing them, a common real-world database technique that keeps deletes cheap
- **Interactive CLI** — run SQL queries directly against the database from the terminal

## Architecture

```
┌─────────────────────────────────────────┐
│              Cli (REPL)                  │
├─────────────────────────────────────────┤
│           QueryExecutor                  │
│   (runs parsed statements against Table) │
├─────────────────────────────────────────┤
│    Tokenizer  →  SqlParser               │
│   (SQL text → structured statements)     │
├─────────────────────────────────────────┤
│              Table                       │
│   (ties records to the B+ Tree index)    │
├───────────────────┬───────────────────────┤
│    BPlusTree      │      Record            │
│  (id → location    │  (serialization/       │
│   index)           │   deserialization)     │
├───────────────────┴───────────────────────┤
│         Page  /  PageAllocator            │
│   (fixed 4KB containers, free page mgmt)  │
├─────────────────────────────────────────┤
│              PageStore                    │
│   (raw disk I/O via RandomAccessFile)     │
└─────────────────────────────────────────┘
```

## Design Decisions & Trade-offs

**Why fixed-size (4KB) pages instead of variable-length writes?**
Disks and OS file caches read/write in fixed blocks regardless of request size. Matching the page size to this means one page read = one disk I/O operation, with no wasted or split reads. It also makes addressing trivial: any page's byte offset is `pageNumber × PAGE_SIZE`, so no lookup table is needed to find where a page lives.

**Why a B+ Tree instead of a hash index?**
B+ Trees keep data sorted, so they support range queries (e.g. "all ids between 10 and 50") efficiently via linked leaf nodes — something a hash index cannot do at all. The cost is a slightly higher constant factor on point lookups compared to a hash table, which was an acceptable trade-off for a general-purpose index.

**Why tombstone deletion instead of physically removing records immediately?**
Physically removing a record mid-page means either leaving a gap (wasting space) or shifting all subsequent records (expensive). Marking a record as deleted is a cheap, constant-time operation; reclaiming the space can happen later via a background compaction process — a pattern used by real databases (e.g. this is conceptually similar to how PostgreSQL's VACUUM works).

**Why does `Page.addRecord()` call `Record.deserialize()` instead of manually parsing bytes?**
Originally it duplicated the record byte-format logic. When the record format changed (adding a deleted flag), this duplicated logic silently broke and caused a buffer overflow bug. Refactoring to have a single source of truth for the record format (in `Record` itself) fixed the class of bug entirely rather than patching the symptom.

## Known Limitations

This is a learning project, and some simplifications were made deliberately:

- **No crash safety** — there's no write-ahead log (WAL), so a crash mid-write could leave a page in an inconsistent state. A production database would log intended writes before applying them.
- **No page reuse after merge** — when B+ Tree nodes merge during a delete, the now-unused page is never reclaimed by the allocator. A free-list would fix this.
- **No concurrency** — single-threaded, single-user only. No locking or multi-connection support.
- **Minimal data types** — records only support an integer id and a string name. No dates, floats, booleans, or multi-column schemas.
- **No query optimizer** — `WHERE id = X` always uses the index; anything else falls back to a full table scan. Real databases choose a strategy based on cost estimation.

## What I Learned

Building this from scratch surfaced a lot of concepts that are easy to gloss over when just *using* a database:
- Why fixed-size pages are the foundational unit almost every storage engine is built around
- How B-Tree/B+ Tree splitting and merging actually works when nodes are addressed by page number instead of memory pointers
- Why length-prefixed encoding is necessary for serializing variable-length data
- The real reason SQL parsing is split into tokenizing and parsing stages
- Why tombstone deletion is preferred over immediate physical deletion in practice

## Tech Stack

Java 17, Maven, no external dependencies beyond the JDK standard library (`java.nio`, `java.io`).

## Running It

```bash
mvn clean package
```
Then run `Cli.java` from your IDE, or via the compiled classpath, and start typing SQL.

## Project Structure

```
src/main/java/com/divyansh/database/
├── Page.java              # Fixed-size disk page container
├── PageStore.java         # Raw file I/O for reading/writing pages
├── PageAllocator.java     # Assigns new page numbers
├── Record.java            # Row serialization (id, name, deleted flag)
├── BPlusTreeNode.java     # B+ Tree node serialization
├── BPlusTree.java         # B+ Tree insert/search/delete with splitting & merging
├── Table.java             # Ties Records to the B+ Tree index
├── Tokenizer.java         # SQL lexer
├── SqlParser.java         # Recursive-descent SQL parser
├── SelectStatement.java   # Parsed SELECT representation
├── InsertStatement.java   # Parsed INSERT representation
├── DeleteStatement.java   # Parsed DELETE representation
├── QueryExecutor.java     # Executes parsed statements against Table
└── Cli.java                # Interactive command-line interface
```
