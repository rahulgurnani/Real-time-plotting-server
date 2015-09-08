import socket               # Import socket module

soc = socket.socket()         # Create a socket object
host = "localhost" # Get local machine name
port = 2004                # Reserve a port for your service.
soc.bind((host, port))       # Bind to the port
soc.listen(5)                 # Now wait for client connection.
BUFSIZE = 4096
conn, addr = soc.accept()     # Establish connection with client.
print ("Got connection from",addr)
while True:
	msg = conn.recv(BUFSIZE)
	if msg == '':
		continue
	print (msg,";;;")
	if msg == "Bye":
		print "done!!"
		break
	
