JMatIO is a JAVA library to read/write/manipulate with Matlab binary
MAT-files.

If you would like to comment, improve, critisize the project please 
email me: wgradkowski@gmail.com 

or visit JMatIO project page at Sourceforge:
http://www.sourceforge.net/projects/jmatio

Subversion Access

This project's SourceForge.net Subversion repository can be checked out through 
SVN with the following instruction set:

svn co https://jmatio.svn.sourceforge.net/svnroot/jmatio/trunk jmatio 

Have fun :)

Wojciech Gradkowski

CHANGE LOG:
[07.14.2011]
+ Added support: Int16, UInt16, Int32, UInt32 array
+ Added a wrapper class for logical
+ Have MLInt8 and MLInt64 extend MLUInt8 and MLUInt64, respectively
+ Miscellaneous clean up and fixes

[05.10.2007]
+ Sparse matrix bugfixes by Jonas Pettersson (LU/EAB)
+ MatFileReader performance enhancements by Eugene Rudoy
+ new MatFileReader methods added

[02.03.2007]
+ Regression bug fixed: Double arrays created natively in Matlab are read 
  incorrectly (reversed byte ordering)

[22.02.2007]
+ Added support:UInt8 array 
+ MAJOR reading performance enhancement - reading is as fast as in Matlab now
+ Removed Log4j references

TODO:
- Other array types (serialized objects (OPAQUE) is done partially)
- Writer performance enhancement
- Documentation and examples
- Organize JUnit tests
- Refactor exceptions
- Make structures and cell arrays more user friendly

NOTE:
Numerical arrays (MLDouble, MLUint8) are now backed by direct ByteBuffers. For 
really BIG arrays the maximum heap size for direct buffers may be modified by 
-XX:MaxDirectMemorySize=<size>


[some.time.2006]
Currently supproted data types are:
+ Double array
+ Char array
+ Structure
+ Cell array
+ Sparse array

