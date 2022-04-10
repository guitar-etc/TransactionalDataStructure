# Transactional Data Structure

***
Read Test.java for use cases

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
1. ~~Handle Multi-Thread~~
2. ~~Shared lock? Exclusive Lock?~~
3. Will not allow. ~~Allow non-transactional operations during a transaction?
4. Copying the entire list is costly. Copy the instance only if a modify operation is performed for better performance.
  1. Or maintain addSet and deleteSet to apply the changes lazily to "impl". Implementing conditional logic for all public methods based on what's in addSet and deleteSet feels too complicated effort.
  2. Come up with some lazy clone technique?
    1. clone on write? clone on 10+ writes?
5. Handle Multi-Thread better.
