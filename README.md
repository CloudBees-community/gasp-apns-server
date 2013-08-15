Gasp! Apple Push Notification Service Demo Server
==================================================

Push data synchronization server for Gasp! iOS demo: uses CloudBees PaaS and Foxweave to provide automatic data sync between the Gasp! server database and iOS CoreData.

The server uses the [Apple Push Notification Service](http://developer.apple.com/library/mac/documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/Chapters/ApplePushService.html#//apple_ref/doc/uid/TP40008194-CH100-SW9) to broadcast Gasp! database update notifications to iOS applications that register with the gasp-apns-server via simple HTTP calls. The FoxWeave Integration service will call via WebHook the REST API exposed by DataSyncService.java, which it turn sends an APNS Push Notification with the record id to registered devices.  The iOS client can then call the Gasp! REST API to retrieve the review data and update its on-device database. The FoxWeave integration is currently set to poll the target database every minute, so for testing purposes you may want to trigger the pipeline manually to see the update notifications immediately.

Setup
-----

1. Set up the Gasp! server and database: see [gasp-server](https://github.com/cloudbees/gasp-server) on GitHub

2. Configure a FoxWeave Integration (Sync) App with a pipeline as follows:
   - Source: MySQL 5 (pointing at your CloudBees MySQL Gasp database)
   - SQL Statement: select #id, #comment, #star, #restaurant_id, #user_id from review where id > ##id
   - Target: WebHook
   - Target URL: http://gasp-gcm-server.<cloudbees_user>.cloudbees.net
   - JSON Message Structure:
`{
    "id":1, 
    "comment":"blank", 
    "star":"three", 
    "restaurant_id":1, 
    "user_id":1
}`
   - Data Mapping: `id->${id}, comment->${comment}` etc

3. Configure Provisioning Profiles and Certificates
   - This [tutorial](http://www.raywenderlich.com/32960/apple-push-notification-services-in-ios-6-tutorial-part-1) explains the steps
   - You will need an iOS Developer Program membership: create the provisioning profile and certificate using the [iOS Developer Portal](https://developer.apple.com/devcenter/ios/index.action)
   - Export your Apple Development iOS Push Services certificate and private key as a p12 keystore file
   - Run the Xcode gasp-apns-client project on a connected device (included in the Provisioning Profile) and note the 64-digit device token.

4. Deploy your FoxWeave Integration App on CloudBees and start it

5. Build this project with: `mvn build install`
   - Add your p12 keystore to src/main/webapp/WEB-INF/classes, default name: GaspApns.p12
   - You will need to download the [javapns](https://code.google.com/p/javapns/) library and install it to your local Maven repository with 'mvn install:install-file -Dfile=JavaPNS_2.2.jar -DgroupId=com.google.code -DartifactId=javapns -Dversion=2.2 -Dpackaging=jar'

6. Deploy to CloudBees: `bees app:deploy -a gasp-apns-server target/gasp-apns-server.war -P APNS_TOKEN=<your device token> -P P12_PWD=<your p12 keystore password>`
   - APNS_TOKEN should be the 64-digit device token
   - P12_PWD should be the password for the p12 keystore containing your iOS Push Services certificate and private key

7. To test the service: 'curl -H "Content-Type:application/json" -X POST http://gasp-apns-server.partnerdemo.cloudbees.net/reviews -d '{ "id":1, "comment":"blank", "star":"three", "restaurant_id":1, "user_id":1 }'


Viewing the Server Log
----------------------

You can view the server log using `bees app:tail -a gasp-apns-server` You should see output similar to this:
'
INFO  DataSyncService - Syncing Review Id: 1
DEBUG DataSyncService - APNS Device Token: <64-digit token>
DEBUG Payload - Adding alert [Gasp! Review Update]
DEBUG ConnectionToAppleServer - Creating SSLSocketFactory
DEBUG ConnectionToAppleServer - Creating SSLSocket to gateway.sandbox.push.apple.com:2195
DEBUG PushNotificationManager - Initialized Connection to Host: [gateway.sandbox.push.apple.com] Port: [2195]: 639cc8fd[SSL_NULL_WITH_NULL_NULL: Socket[addr=gateway.sandbox.push.apple.com/17.172.233.65,port=2195,localport=47982]]
DEBUG PushNotificationManager - Building Raw message from deviceToken and payload
DEBUG PushNotificationManager - Built raw message ID 1 of total length 84
DEBUG PushNotificationManager - Attempting to send notification: {"aps":{"alert":"Gasp! Review Update"}}
DEBUG PushNotificationManager -   to device: <64-digit token>
DEBUG PushNotificationManager - Flushing
DEBUG PushNotificationManager - At this point, the entire 84-bytes message has been streamed out successfully through the SSL connection
DEBUG PushNotificationManager - Notification sent on first attempt
DEBUG PushNotificationManager - Reading responses
DEBUG PushNotificationManager - Closing connection
'


