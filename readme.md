# APN-PROXY
The apn proxy project aims to provide a total solution to access the open internet bypass some firewalls, especially for mobile devices such as iPhone, iPad, or Android devices.

## Inside Server
The inside server acts as a http proxy server to user-agent. The inside server will be depolied behind the firewall.

## Outside Server
The outside server will fetch the data from the original target server outside the firewall, usually it will be deploied outside the China mainland.

## The Structure
	User-Agent <---> Inside Server <-- The Fucking Firewall --> OutSide Server <---> Original Target Server

The connection between insede sever and outside sever will be encrpted.