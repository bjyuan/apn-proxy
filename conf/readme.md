configration example

```
<apn-proxy>
	<!--
	<listen-type>3des</listen-type>
	-->
	
	<listen-type>plain</listen-type>
	
	<triple-des-key></triple-des-key>
	
	<!-- 
	<listen-type>ssl</listen-type>
	<key-store>
		<path>conf/keystore.ks</path>
		<password></password>
	</key-store>
	-->
	
	<port>8700</port>
	<thread-count>
		<boss>50</boss>
		<worker>200</worker>
	</thread-count>
	
	<pac-host>localhost</pac-host>
	
	<use-ipv6>false</use-ipv6>
	
	<remote-rules>
		<rule>
			<remote-host></remote-host>
			<remote-port>8700</remote-port>
			<apply-list>
				<original-host>google.com</original-host>
				<original-host>twitter.com</original-host>
				<original-host>youtube.com</original-host>
				<original-host>facebook.com</original-host>
			</apply-list>
		</rule>
		<rule>
			<remote-host></remote-host>
			<remote-port>8700</remote-port>
			<apply-list>
				<original-host>t66y.com</original-host>
				<original-host>wordpress.com</original-host>
			</apply-list>
		</rule>
	</remote-rules>

</apn-proxy>

```
