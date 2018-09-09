

Pacific Data Services - Sales Reporting
=======================================

Introduction
------------

Pepsi-Frito uses Custom Data solutions to record and report vending sales.  I am extremely
familiar with this operation as I taught Custom Data Solutions the technology for 25 years
and functioned as their first Chief Information Officer 

I redesigned the entire process in the two years I was an employee.

More in :doc:`Contributions`

Benefits
--------

* Audit the data and rebate calculations. :doc:`Audit`

* Get answers without asking questions :doc:`Answers`

* Custom Reporting

* Online analytical processing

* Startup is negligible
  
  * Just need copies of the files the distributors already report :doc:`cds_record_layout`

  * A product master file

  * No computing resources, no long term contracts 


Once installed:

#. Reporting data is loaded into staging tables

#. Verification of compliance with requirements and self consistency of data is ensured

#. Data is posted to history tables

#. Customer addresses are validated

#. Exceptional conditions are identified and reported

#. Rebate Calculations are made and reported

#. Rebate amounts are computed and an ACH file for transmission to a bank for payment is created

#. Spreadsheets for sales analysis are created

Functioning Real World Usage
----------------------------

Any distributor currently writing files in the CDS reporting format can easily load files 
into a local database in order to analyze the data.

We have provided many analyses that report data worthy of further inspection.

This approach is our "answers to questions you should have asked".


Audit
=====

Pacific Data Services will audit the data and rebate calculations for your distributors.

Findings
--------

Item Accuracy
*************
We will compare the items assigned by Custdata to the actual items sold by the distributors.

This affect sales history and rebate calculations

Addresses
*********
Report incorrect addresses and undeliverable addresses.

Rebate
******
Calculate the actual rebate for the top 50 rebate amounts issued versus what Custdata
computed.

Report augmentation
-------------------

Answers to Questions you should have asked
******************************************
Rather than spend time on the Custdata website creating spreadsheets and trying to spot
interesting and unusual data we will create *Alert Reports* and notify you that 
there is research warranted.

Examples
&&&&&&&&

* Newly introduced items not being bought by distributor

* Top selling items by category not being bought by distributor

* Top selling items by category not being bought by a vending operator

* Product declining sales for a vending operator

* Dropped products for a vending operator

* Parasitic promotion effect (Items not on promotion having declining sales during a promotion
as slot contention has had a parasitic effect.




Sales
=====


Required Data
-------------
Distributors already report their data to custdata.com.

They have to do nothing but send us the same data files, no additional work.

Benefits
--------

Manufacturers
*************

Benefits
&&&&&&&&

* Far lower costs
* Faster and Cheaper Custom Reports Generation

* Improved Data Quality
* Far Lower Costs
* Data Identified for them

  Currently the manufactures have to click through the Custdata website and create a spreadsheet and see if there is
  interesting data.

  We can analyze the data and inform them of interesting conditions, for example

 New Reports
 &&&&&&&&&&&

  * New Vending Operators
  * Vendors with Declining Sales
  * Vendors not carrying products

New Capabilities
&&&&&&&&&&&&&&&&

* Address Correction
* 


Distributors
************

* Ability to create an operator facing website


Vending Operators
*****************

* Find new  products to be offered




Background
==========
.. toctree::
   :maxdepth: 2
   :caption: Contents:

     
   Analyze      
   Answers
   Audit
   Benefits
   Contributions
   ProductOffering
   ProjectDescription
   Technologies
   cds_record_layout

