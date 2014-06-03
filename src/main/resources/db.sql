--database settings
--ALTER DATABASE DATETIMEFORMAT yyyy-MM-dd'T'HH:mm:ss.sssZ

--Address
CREATE CLASS Address;
CREATE PROPERTY Address.address_lines EMBEDDEDLIST STRING;
CREATE PROPERTY Address.locality STRING;
CREATE PROPERTY Address.sanitized_locality STRING;
CREATE PROPERTY Address.city STRING;
CREATE PROPERTY Address.sanitized_city STRING;
CREATE PROPERTY Address.state STRING;
CREATE PROPERTY Address.country STRING;
CREATE PROPERTY Address.pincode STRING;

alter property Address.locality mandatory=true;
alter property Address.locality notnull=true;
alter property Address.sanitized_locality mandatory=true;
alter property Address.sanitized_locality notnull=true;
alter property Address.city mandatory=true;
alter property Address.city notnull=true;
alter property Address.sanitized_city mandatory=true;
alter property Address.sanitized_city notnull=true;
alter property Address.state mandatory=true;
alter property Address.state notnull=true;
alter property Address.country mandatory=true;
alter property Address.country notnull=true;
alter property Address.pincode mandatory=true;
alter property Address.pincode notnull=true;

alter property Address.city custom cities = Bangalore,Hyderabad
alter CLASS Address custom identifiers = pincode|sanitized_city,sanitized_locality

--Timing
CREATE CLASS Timing;
CREATE PROPERTY Timing.opening_timings EMBEDDEDLIST STRING;

--Description
CREATE CLASS Description;

--Facility
CREATE CLASS Facility;
CREATE PROPERTY Facility.description STRING;
CREATE PROPERTY Facility.how_to_avail STRING;
CREATE PROPERTY Facility.type STRING;
CREATE PROPERTY Facility.where STRING;

alter property Facility.type mandatory=true;
alter property Facility.type notnull=true;

--Brand
CREATE CLASS Brand;
CREATE PROPERTY Brand.description EMBEDDED Description;
CREATE PROPERTY Brand.name STRING;
CREATE PROPERTY Brand.sanitized_name STRING;

alter property Brand.name mandatory=true;
alter property Brand.name notnull=true;
alter property Brand.sanitized_name mandatory=true;
alter property Brand.sanitized_name notnull=true;

alter CLASS Brand custom identifiers = sanitized_name;

--Mall
CREATE CLASS Mall;
CREATE PROPERTY Mall.address LINK Address;
CREATE PROPERTY Mall.description EMBEDDED Description;
CREATE PROPERTY Mall.facilities EMBEDDEDLIST Facility;
CREATE PROPERTY Mall.timing EMBEDDED Timing;
CREATE PROPERTY Mall.name STRING;
CREATE PROPERTY Mall.sanitized_name STRING;

alter property Mall.name mandatory=true;
alter property Mall.name notnull=true;
alter property Mall.sanitized_name mandatory=true;
alter property Mall.sanitized_name notnull=true;

alter CLASS Mall custom identifiers = sanitized_name,address

--Category
CREATE CLASS Category;
CREATE PROPERTY Category.name STRING;
CREATE PROPERTY Category.sanitized_name STRING;

alter property Category.name mandatory=true;
alter property Category.name notnull=true;
alter property Category.sanitized_name mandatory=true;
alter property Category.sanitized_name notnull=true;

alter CLASS Category custom identifiers = sanitized_name

--Offer
CREATE CLASS Offer;

--Sell
CREATE CLASS Sell;
--CREATE PROPERTY Sell.category LINK Category;
--CREATE PROPERTY Sell.brand LINK Brand;
--CREATE PROPERTY Sell.offers LINKLIST Offer;

--alter property Sell.category mandatory=true;
--alter property Sell.category notnull=true;
--alter property Sell.brand mandatory=true;
--alter property Sell.brand notnull=true;

--Store
CREATE CLASS Store;
CREATE PROPERTY Store.address LINK Address;
CREATE PROPERTY Store.mall LINK Mall;
CREATE PROPERTY Store.brand LINK Brand;
CREATE PROPERTY Store.sells LINKLIST Sell;

alter property Store.address mandatory=true;
alter property Store.address notnull=true;
alter CLASS Store custom identifiers = mall,brand