# ExtDriveMgmt
A small tool to browse the content of external hard drives when they are not attached to your PC.

## Instructions
Open a command line, go to your hard drive and enter

    dir /s >dir_Drive1.txt

(Alternatively, use the provided "dir.bat" from the root of your hard drive)

Copy all .txt files to this folder and execute the tool.

## Functionality
* Show general information about all drives
* Browse content of each drive in an Explorer-like way
* Browse content in a combined view
* Shows for each file/folder on which drive(s) they are
* Search for files/folders

## Screenshots
Show general information about all drives
![Screenshot Drive View](/Screenshot_DriveView.png)

Browse content of each drive, or combined, in an Explorer-like way
![Screenshot File View](/Screenshot_FileView.png)

Search for files/folders
![Screenshot Search View](/Screenshot_SearchView.png)

## Limitations and TODOs
* Only german is supported in parsing the output of "dir". This can be adapted in src/mgmt/DirParser.java
* Codepage not adapted; this can cause some characters to be wrong
