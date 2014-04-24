--database settings
--ALTER DATABASE DATETIMEFORMAT yyyy-MM-dd'T'HH:mm:ss.sssZ

--classes
--Node
--create class _BB_NodeVertex extends V;

--Node class should be abstract but we cannot declare it as abstrat due the index on the id field
--create class _BB_Node  extends ORestricted;
--create property _BB_NodeVertex._node link _BB_Node;
--create property _BB_Node._creation_date datetime;
--create property _BB_Node._links link _BB_NodeVertex;
--create property _BB_Node.id String;

--user
--create class _BB_User extends _BB_Node;
--create class _BB_UserAttributes extends ORestricted;
--create property _BB_User.visibleByAnonymousUsers link _BB_UserAttributes;
--create property _BB_User.visibleByRegisteredUsers link _BB_UserAttributes;
--create property _BB_User.visibleByFriend link _BB_UserAttributes;
--create property _BB_User.visibleByTheUser link _BB_UserAttributes;
--create property _BB_User._audit embedded;
--create property _BB_User.user link ouser;


--admin user
--insert into _BB_User set user = (select from ouser where name='admin'), _links = (insert into _BB_NodeVertex set _node=null), _creation_date = sysdate(), signUpDate = sysdate();
--update _BB_NodeVertex set _node=(select from _BB_User where user.name='admin');


--Address
CREATE CLASS Address;
CREATE PROPERTY Address.address_lines EMBEDDEDLIST STRING;
CREATE PROPERTY Address.locality STRING;
CREATE PROPERTY Address.city STRING;
CREATE PROPERTY Address.state STRING;
CREATE PROPERTY Address.country STRING;
CREATE PROPERTY Address.pincode STRING;

alter property Address.locality mandatory=true;
alter property Address.locality notnull=true;
alter property Address.city mandatory=true;
alter property Address.city notnull=true;
alter property Address.state mandatory=true;
alter property Address.state notnull=true;
alter property Address.country mandatory=true;
alter property Address.country notnull=true;
alter property Address.pincode mandatory=true;
alter property Address.pincode notnull=true;

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

--Shop
CREATE CLASS Shop;
CREATE PROPERTY Shop.description EMBEDDED Description;
CREATE PROPERTY Shop.name STRING;

alter property Shop.name mandatory=true;
alter property Shop.name notnull=true;

--Mall
CREATE CLASS Mall;
CREATE PROPERTY Mall.address LINK Address;
CREATE PROPERTY Mall.description EMBEDDED Description;
CREATE PROPERTY Mall.facilities EMBEDDEDLIST Facility;
CREATE PROPERTY Mall.timing EMBEDDED Timing;
CREATE PROPERTY Mall.name STRING;

alter property Mall.name mandatory=true;
alter property Mall.name notnull=true;

--Brand
CREATE CLASS Brand;

--Category
CREATE CLASS Category;

--Offer
CREATE CLASS Offer;

--Sell
CREATE CLASS Sell;
CREATE PROPERTY Sell.category LINK Category;
CREATE PROPERTY Sell.brand LINK Brand;
CREATE PROPERTY Sell.offers LINKLIST Offer;

alter property Sell.category mandatory=true;
alter property Sell.category notnull=true;
alter property Sell.brand mandatory=true;
alter property Sell.brand notnull=true;

--ShopMall
CREATE CLASS ShopMall;
CREATE PROPERTY ShopMall.mall LINK Mall;
CREATE PROPERTY ShopMall.shop LINK Shop;
CREATE PROPERTY ShopMall.sells EMBEDDEDLIST Sell;