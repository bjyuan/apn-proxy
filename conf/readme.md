configration example

```xml

<?xml version="1.0" encoding="UTF-8" ?>
<apn-proxy>

	<listen-type>plain</listen-type>

	<port>9000</port>
	<thread-count>
		<boss>50</boss>
		<worker>200</worker>
	</thread-count>

	<pac-host>127.0.0.1</pac-host>

	<use-ipv6>false</use-ipv6>
	
	<remote-rules>
		<rule>
			<remote-listen-type>3des</remote-listen-type>
			<remote-3des-key>{your_remote_3des_key}</remote-3des-key>
			<remote-host>{your_remote_apn_proxy_host_with_3des}}</remote-host>
			<remote-port>9000</remote-port>
			<apply-list>
				<original-host>google.com</original-host>
				<original-host>twitter.com</original-host>
			</apply-list>
		</rule>
	</remote-rules>
</apn-proxy>

```
