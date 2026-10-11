# SQL commands to create and populate the credentialsDB database for Project Three
# CNT 4714 - Summer 2026

#delete the database if it already exists
drop database if exists credentialsDB;

# create a new database named credentials DB
create database credentialsDB;

# switch to the new database
use credentialsDB;

# create the schemas for the userCredentials relation in this database
create table usercredentials (
    login_username varchar(25),
    login_password varchar(25),
	primary key (login_username)
);

# NOTE: you will need to change your root user password value
insert into usercredentials values ("root", "rootMAC1$");
insert into usercredentials values ("client", "client");
insert into usercredentials values ("dataentry", "dataentry");
insert into usercredentials values ("theaccountant", "theaccountant");

# uncomment the following line if you want to see the results of creating  database
# select * from usercredentials;
