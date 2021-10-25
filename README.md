# Transactional Data Structure

***

Jdk data structures that supports "commit" and "rollback" like DBs.

There are cases where the business process eventually gets denied and a series of rollback should occur.
One could be lucky enough to simply discard the instances.
Others are not so lucky and have to revert all the changes they made and move on to the alternative business process.

***

### Currently....
10-23
1. Initial commit.
2. Very naive implementation for ArrrayList.
  1. Copy the instance, do all operations on that copied instance, and replace the original instance with the copied instance if "commit".
  2. If "rollback", just discard the copied instance.
3. Multiple transactions can be ongoing.
  1. Inner transaction behave just the same.
  2. Outer transaction will end the inner transactions the same way it ends, when it ends.

### TODO.
1. Handle Multi-Thread
2. Shared lock? Exclusive Lock?
  1. Copy the instance only if a modify operation is performed.
4. Allow non-transactional operations during a transaction?
5. Copying the entire list is costly.
