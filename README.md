QuickJetty
==========

QuickJetty is a simple configuration for a jetty server.

You can just drop the jar in a directory you want to serve and double click.

You can run it from the command line to start up an http server with jsp support anywhere.  It also supports proxying so that you can use it when developing against, e.g. couchdb or some other service.

The syntax is:

    java -jar quickjetty.jar [<path to serve> [<port> ["prefix->proxyTo" ...]]]

An example would be

    java -jar quickjetty.jar . 8080 "db->http://localhost:5984/mydb"

Which would run the web server on port 8080, serving the current directory, but proxying requests that start with /db to the servering running on 5984.

It has sensible defaults and is executable, so if you want to just serve a particular directory, you can just copy the jar in and double click on it. It will serve from localhost:8181. The command line equivalent is

    java -jar quickjetty.jar

You can download the most recently committed built version from [here](https://github.com/kybernetikos/QuickJetty/blob/master/builtVersion/quickjetty.jar?raw=true).

I recently came across [jtty](https://github.com/stephenh/jtty) which is a similar project that provides a nice way to serve webapps from the command line.
