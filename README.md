
FACEBOOK PROJECT
Ishan Patwa UFID: 65461485
Juthika Das UFID: 71735283


INSTRUCTIONS FOR RUNNING THE PROJECT:

This project has the following Folder structure 
--Facebook3
--Executables
--Readme.pdf

Facebook3 is a sbt project contains all the sbt config files
Executables contains Jar files.
Readme is the file explaining the architecture and performance ( this file )

This Project can be run in two ways.

First Way :Go to the Executables section and run the Facebook-Server.jar followed by Facebook-Client.jar.
Facebook-Server.jar doesn't take any arguments. to run Facebook-Client.jar you have to give one argument that will be an integer specifying the load  from the Client to server.
Example usage :
run : java -jar Facebook-Server.jar 
java -jar Facebook-Client.jar 100 ( 100 is the value we have used in our experiments)


Second way :  IF SBT is installed , if sbt is available on the machine. you can go to the project folder i.e Facebook3 and run using the “run” command.

NOTE: Since we have two entry points for client and Server.
to run server first enter the following.
sbt 
run 
select Server ( option 2)

to run client enter the following command.
sbt 
run 
select facebookClient.Client ( option 3)
 


NOTE : The output on the server is number of requests processed each second. 
We have refrained from printing timeline/profiles/posts etc as they just hamper the performance.
ARCHITECTURE

The system is divided into the client side, interface and the server side. The client side consists of the ClientService.scala which is the class that initiates get and post request actors that are instantiated through the GETRequestClient and the POSTRequestClient classes. The gETRequestClient class sends HTTP requests such as getWall to retrive the wall, getProfile to get the profile of a mentioned userId etc. We are using the Spray http library to implement this functionality. 

To connect the client and the server side, we have an interface that we define in a class called Master.scala. This class receives the get and post requests from the client and the server. 

The main class is server.scala. It performs 2 major tasks:
Start all the services, which involves initializing all the service hashmaps and the routers. We have 5 service routers called PostServiceRouter, WallServiceRouter, PhotoServiceRouter, FriendListRouter and ProfileServiceRouter. On the same lines, we have 5 hashmaps. 
We’re considering the wall to be a universal hashmap. The wall basically consists of all posts from all users in the system. The WallHashMap is therefore a nested hashmap. 
The profile hashmap is used to store a user profile, associated with the userId of a user. The profile has fields and values. The fields, in our case, are random strings but they can be fields such as name, school, age, date of birth and other personal information.
 The FriendsListMap is a Simple hashmap of a userId as they key and a list of friends as its value. This gets updates when a friend request is sent. 
Then we have the photoURLMap. The photoURLMap is used during the service of sending photos to a friend’s wall. So this hashmap consists to the receiverId, the photo URL and an album name that the photo is meant to belong to.
To initialize the interface that is defined in Master.scala

On the server side, we have two set of classes i.e the serviceRouters and the services. The serviceRouters create a router and route the message to the main service class. To make things unabmiguous, we have a one-to-one mapping between the service routers and the services. So the serviceRouters comprise of the WallServiceRouter, ProfileServiceRouter, FriendListRouter, PhotoServiceRouter and PostSeriveRouter. These route messages to the WallService, ProfileService, FriendListService, PhotoService and the PostService respectively. 


SERVICES

The services are where we’re updating all the hashmaps, either globally in case of the WallHashMap or depending on the userId in case of all other HashMaps. Depending on the value fields in the hashMaps, which are new hashmaps in case of the WallHashMap, PostHashMap, ProfileHashMap and PhotoURLHashMap or a list of userIds in case of FriendListHashMap, we append the hashmaps by adding the request values. On completion of these updates, the interface, i.e Master.scala gets a completion message. 

To check the load for the system, we have defined a service called the LoadMonitorService. The LoadMonitorService keeps track of all kinds of threads that are being used when a request is made, so we have postThreads, wallThreads, addFriendThreads, photoThreads and getThreads. At the same time we’re also keeping track of the total number of threads in use. This is maintained using a variable called allThreads which gets updated whenever a request is being processed. The load monitor tells us the number of threads being used per second. 


Performance :

As mentioned below, we set up a load Monitor service which kept track of all the load on the Akka Facebook server  below is a performance chart for  a session. 

As we can clearly see, We are averaging around 18,000 request per second which is a very good performance measure for such a system. This session was run on Lin machines of the University Lab with the following specifications:

8 core Machine with the following specification:
Architecture:          x86_64
CPU op-mode(s):        32-bit, 64-bit
Byte Order:            Little Endian
CPU(s):                8
On-line CPU(s) list:   0-7
Thread(s) per core:    2
Core(s) per socket:    4
Socket(s):             1
NUMA node(s):          1
Vendor ID:             AuthenticAMD
CPU family:            21
Model:                 2
Stepping:              0
CPU MHz:               1400.000
BogoMIPS:              7584.70
Virtualization:        AMD-V
L1d cache:             16K
L1i cache:             64K
L2 cache:              2048K
L3 cache:              8192K
NUMA node0 CPU(s):     0-7
