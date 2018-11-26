# Optimization

Originally, we use preparedStatement to run the sql. And to check if the data is duplicate, we have to run another sql again (select...where...). We then use another method to check duplicate. We first store all the data, say table movies, in a HashMap. It only need to run once. After we got this HashMap, we can check duplicate in O(1) time, so it improves the performance a lot. Also, since the sql is the same, only parameters are different, we use batch to execute the sql. When using batch, the database only need to connect once after all the sql command is ready, so it reduce the I/O time to fetch the database. 

Origin method takes over 10 minutes to run all three xml files. After improving the check method, it takes around 5 minutes. After batch method,
it only takes less than 5 second.

## Final result:
mains243.xml: 5520 millisecond

actors63.xml: 2457 millisecond

casts124.xml: 2803 millisecond

