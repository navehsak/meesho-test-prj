# meesho-test-prj


Shipment Tracking Module

We need to optimise for shipment tracking. Currently, whenever an order is marked shipped by supplier, we hit the carrier partner API to manifest the order and carrier partner gives us the waybill number to track. We have a cron job which takes all the waybills which are currently in transit state (not yet delivered) and hits the partner API 1 by 1 and fetches the status of the shipment and updates in database (if status is changed from last status). Cronjob runs every 2 hours. Now this approach becomes unoptimised as the number of orders grow as we are bombarding the partner system every 2 hours and our system is also taking the hit at particular intervals. 


Design the system to do this is in a scalable fashion such that we don’t bombard the partner system at once and rather distribute the tracking of shipments over the window of 2 hours. Think in the terms that we need to check the status for a particular shipment every 2 hours i.e if a shipment is created at 11:59 PM, we check it’s status at 1:59 PM, 3:59 PM, 5:59 PM and so on, until the status is delivered. 



Assume the following structure of the database where the shipments are currently saved:- 



id, awbNumber, carrierId, status, createdTimestamp, updatedTimestamp



Lets have a discussion after you complete this design. Only then proceed to implementing a subset of this (to be discussed over design review call)

   

Guidelines for implementation

Feel free to use some micro framework. Don't use full fledged bloated stuff that abstracts away everything in the problem statement.



Use OOP and keep architecture clean. Share the code with us in a github repo. Also preferably, share it in the beginning itself so that we can monitor progress. 



Bonus points for handling edge cases and errors.
