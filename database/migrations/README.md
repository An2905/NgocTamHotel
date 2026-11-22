# Database migrations

Add future schema changes here using `YYYYMMDD_description.sql` filenames. Never edit an already-deployed migration.

`20261108_multi_room_bookings.sql` upgrades the previous schema with
`room_bookings.room_id` to the new `booking_rooms` association table. Run it once
against an existing database before using the updated booking code. It copies
existing room assignments before removing the old column. MySQL DDL commits
implicitly, so keep a database backup before applying it.

For a new database, use `database/scripts/schema.sql` and then
`database/scripts/seeding.sql`; do not also run this migration.

`20261108_rename_booking_note.sql` renames an existing `room_bookings.mote`
column to `note`. Run it only when the old column still exists. New databases
already use `note` in `schema.sql`.

`20261112_align_booking_customer_payment.sql` runs once on an existing UUID
database after the applicable `20261108` migrations. It adds the customer phone
lookup index, aligns dates and payment columns, and permits multiple payment
attempts per room booking. Existing payment `TIME` values are assigned the
booking creation date; verify historical paid dates against payment records.
Use `schema.sql` for a new database instead of this migration.


`20261112_guest_booking_snapshots.sql` follows the alignment migration on existing databases. It copies customer contact data into each room/bike booking, removes customer foreign keys and drops customer_information. Back up before running; verify the two contact checks before dropping the old columns/table. Existing bike room_id is retained only as legacy data, because a physical room cannot identify a unique past room booking. New schemas use optional room_booking_id instead.

`20261112_payos_order_code.sql` makes the unique numeric PayOS code AUTO_INCREMENT while preserving existing codes. Apply after the alignment migration, before requesting payment links with the new backend.

`20261113_room_holds.sql` adds ten-minute room holds and the booking payment deadline.
Apply it once to an existing database after the 20261112 migrations, before deploying
the new booking backend. New databases already contain these tables and column in
`database/scripts/schema.sql`.
