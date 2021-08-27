# AltSource New Accounts - RPFS Rail Worker

This project is the Android RPFS Rail Worker app used to review work schedules, perform job setup, enter daily work reports and respond to flash audit requests.  

# Important Information on Branch Variants
In November of 2020, RailPros decided to switch out of the Mobile Device Management (MDM) system in place for distribution.  The desire was to keep both MDM systems in place for a time.  Due to the new system wanting to use the Enterprise Play Store, this new MDM required that the mobile app change its namespace / application id.  This is due to the version used with the old MDM was also in the Play Store (just not the Enterprise Play Store).  While I believe they could have migrated their existing Play Store account, they were not interested in that approach.

Old Application Id: com.railprosfs.railsapp
New Application Id: com.railprosfs.railsapp 

As such, two parallel code branches emerged.  Those two branches must be maintained and changes made to both until the old MDM is retired.  As a convention, the even number releases are to use the new namespace and the odd number releases will use the old namespace.

The first release to use the new application id: Release_0.64.  All odd numbered releases until ?? use the old application id.


## Documentation

See AltSource New Accounts for any documentation or specification.






