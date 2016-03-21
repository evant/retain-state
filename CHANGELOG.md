### 0.3
- Renamed `RetainState.get()` to `RetainState.from()` to make the different between the static
 method and the instance method `get()` more clear.
- Added optional fragment support, though the same concept can be applied to any hierarchical system
as long you can derive a unique id for each item at the same level.
- Fixed loader start() restarting if it completed but was not canceled.

### 0.2
- Some api improvements to loader, added some battery-included loaders for AsyncTasks, Cursors, and 
rxjava. These are optional dependencies.

### 0.1
- Initial Release.
