# Client creation and permission assignment script for CNT 4714 Project 3 - Summer 2026
#
# IMPORTANT NOTE: Run all database creation scripts before running this script.
#
# Delete the client1, client2,  project3app, and theaccountant users created for Project 3
#  NOTE: If any of these users are currently connected to the MySQL server, terminate their connections prior to running this script.
#  If any of these clients are currently connected, the drop user command will not take effect until the that user's connection is terminated.
#
#  Revoke all privileges, grant option, drop user from "client1";
drop user if exists "client1";
# Revoke all privileges, grant option, drop user from "client2";
drop user if exists "client2";
# Revoke all privileges, grant option, drop user from "project2app";
drop user if exists "project2app";
#Revoke all privileges, grant option, srop user from "theaccountant";
drop user if exists "theaccountant";

# Create the client users for Project 3
# As with Project 3, your root user is already created and should have all permissions on all schemas
create user if not exists "client" identified with sha256_password by "client";
create user if not exists "dataentry" identified with sha256_password by "dataentry";
create user if not exists "systemapp" identified with sha256_password by "systemapp";
create user if not exists "theaccountant" identified with sha256_password by "theaccountant";

# Assign privileges to the end users for Project 3 - Again, root user already has all privileges
grant select on project3.* to client;
grant select, insert, update on project3.* to dataentry;
grant execute on project3.* to theaccountant;
grant all on credentialsDB.* to systemapp;

