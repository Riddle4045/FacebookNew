
#Facebook Simulation using Akka and Spray.Io

Main Contributors : [Ishan Patwa](http://riddle4045.github.io/blog/), [Juthika Das](http://djuthika.github.io/)

##How to run the project:

This project has the following Folder structure 
-Facebook3
-Executables
-README.md


Facebook3 is a sbt project contains all the sbt config files Executables contains Jar files for those not familiar with working's of SBT.


There are two ways to run the project:.

First Way :Go to the Executables section and run the Facebook-Server.jar followed by Facebook-Client.jar.
Facebook-Server.jar doesn't take any arguments.
Facebook-Client.jar takes  one argument that will be an integer specifying the load  from the Client to server.
Example usage :
```
run : java -jar Facebook-Server.jar 
java -jar Facebook-Client.jar 100 ( 100 is the value we have used in our experiments)
```

Second way : ** IF SBT is installed**
 if sbt is available on the machine. you can go to the project folder i.e Facebook3 and run using the “run” command.

NOTE: Since we have two entry points for client and Server.
to run server first enter the following.
```
sbt 
run 
select Server ( option 2)
```

to run client enter the following command.

```
sbt 
run 
select facebookClient.Client ( option 3)
 
```

>NOTE : The output on the server is number of requests processed each second. 
We have refrained from printing timeline/profiles/posts etc as they just hamper the performance.


##ARCHITECTURE

The project simulates Facebook API's such as `Posts Messages`,` Walls`,`Photos`, `Timelines` and `Albums`. We have also simulated a Client API that mimics the load of actuall facebook users on the server, the client side is written using [Spray-can](http://spray.io/).

To optimize performance on each side, all the API's simulated have their corresponding serviceRouters ( Akka Routers ) which distribute the incoming HTTP request among routees. These service routers are Akka based actors each handling one part of the API. 

The Client side sends request to a Master.scala Interface actor which intercepts those requests using routes as defined in spray-routing and forwards the requests to the required ServiceRouter.


##Class Strucute.

The main class is server.scala. It performs 2 major tasks:
*Start all the services, which involves initializing all the service hashmaps and the routers. We have 5 service routers called PostServiceRouter, WallServiceRouter, PhotoServiceRouter, FriendListRouter and ProfileServiceRouter. On the same lines, we have 5 hashmaps. 
*We’re considering the wall to be a universal hashmap. The wall basically consists of all posts from all users in the system. The WallHashMap is therefore a nested hashmap. 
*The profile hashmap is used to store a user profile, associated with the userId of a user. The profile has fields and values. The fields, in our case, are random strings but they can be fields such as name, school, age, date of birth and other personal information.
*The FriendsListMap is a Simple hashmap of a userId as they key and a list of friends as its value. This gets updates when a friend request is sent. 
*Then we have the photoURLMap. The photoURLMap is used during the service of sending photos to a friend’s wall. So this hashmap consists to the receiverId, the photo URL and an album name that the photo is meant to belong to.
To initialize the interface that is defined in Master.scala

On the server side, we have two set of classes i.e the serviceRouters and the services. The serviceRouters create a router and route the message to the main service class. To make things unabmiguous, we have a one-to-one mapping between the service routers and the services. So the serviceRouters comprise of the WallServiceRouter, ProfileServiceRouter, FriendListRouter, PhotoServiceRouter and PostSeriveRouter. These route messages to the WallService, ProfileService, FriendListService, PhotoService and the PostService respectively. 


##Services

The services are where we’re updating all the hashmaps, either globally in case of the WallHashMap or depending on the userId in case of all other HashMaps. Depending on the value fields in the hashMaps, which are new hashmaps in case of the WallHashMap, PostHashMap, ProfileHashMap and PhotoURLHashMap or a list of userIds in case of FriendListHashMap, we append the hashmaps by adding the request values. On completion of these updates, the interface, i.e Master.scala gets a completion message. 

To check the load for the system, we have defined a service called the `LoadMonitorService`. The LoadMonitorService keeps track of all kinds of threads that are being used when a request is made, so we have postThreads, wallThreads, addFriendThreads, photoThreads and getThreads. At the same time we’re also keeping track of the total number of threads in use. This is maintained using a variable called allThreads which gets updated whenever a request is being processed. The load monitor tells us the number of threads being used per second. 


##Performance 

As mentioned below, we set up a load Monitor service which kept track of all the load on the Akka Facebook server  below is a performance chart for  a session. 

![Request Rate](resources/Untitled.png?raw=true "Title")

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
