# CloudClient
Java Android Cloud Client paired with [Java Android Cloud Server](https://github.com/Adrian-Gonzalez-Madruga/CloudServer) for usage from the Client. This Client interface will on startup prompt the user to login or create a new account. Systems are in place to prevent duplicate creation, authentication, and more on both front and back end. After logging in the users profile of stored files and folders are displayed and may be downloaded or the user may upload their own projects to the server.

## Getting Started
The Cloud Client after cloning has a small edit that must be changed.
CloudClientV3\app\src\main\java\com\example\adrian\cloudclientv3\MainActivity.java line 48
```java
public static final String STR_IP_ADDRESS = "10.16.6.188";
```
Must be changed with the local ip address of the device running the server.
After the above change the [Java Android Cloud Server](https://github.com/Adrian-Gonzalez-Madruga/CloudServer) must be running for the Cloud Client to connect and share data.

## View Project
Click below to view video of the project being used

[![CloudClientr Youtube](http://img.youtube.com/vi/x-qbk-35JNc/0.jpg)](http://www.youtube.com/watch?v=x-qbk-35JNc "CloudClient")

## Built With

* [Android](https://developer.android.com/) - Framework
* [JSON](https://www.json.org/) - Display Cloud Files

## Flaws, Fixes, and TODO

* Minor code redundancy - Reformatting and applying design pattern to improve efficiency
* Code to select file cannot be over 9 - Use Regex to cut code segments alowwing for unlimited length
* Notifications Sometimes Will Stay In Downloading State Or Cannot Be Removed
* ReSend JSON File Directory After Upload To See New File (At Current Must LogOut and LogIn to Recieve File)
* Add Catching For Loss Of Connection While Uploading/Downloading
* Add Folder Creation Button - TODO
* Add Delete Item Button- TODO
* Add Encryption Whilst Sending Credentials and Files - TODO
* Add Errors From Server - TODO


## Authors

* **[Adrian Gonzalez Madruga](https://github.com/Adrian-Gonzalez-Madruga)** - *Server Communication, The Manipulation, Display, Download, and Upload of Files*
* **[Connor Clarkson](https://github.com/clarksoc)** - *Login Page, Recycler View Elements, Notifications, Asynchrounous Task Managment*
