
PEKGD
=====

Portable EKG (Electrocardiography) Device  


Authors:  
Nick Caley  
Adam Metlzer  


=== How do I use this? ===


Requirements:  
* Apache Ant  
* Android SDK  


To compile:  
run `ant` in the project directory for a list of ant targets  


To transfer to device:  
make sure adb daemon is running as root  
You can ensure this by doing the following:  
`sudo adb kill-server`
`sudo adb start-server`


Plug your Android device into a USB port


Enable USB Debugging on your Android device:   
`Settings > Applications > Development > Android Debugging`


You run the following command to get a list of connected devices  
`adb devices` 


If your device is showing up like  
`?????????? BlahBlah`

Then you need to make sure adb is running as root

If it shows up as  
`asd4t4gfgf offline`

Then your device needs to accept the connection from the computer, you should have a dialog to accept on your device  


To transfer and install the app to your device, you can either run one of the ant install targets  
`ant installd`  
or run the following command (the -r is for reinstalling the app):  
`adb install [-r] <path to .apk file>`  

The app should then be on your device

