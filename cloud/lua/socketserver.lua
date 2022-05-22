#!/usr/bin/lua

local socket = require("socket")

host, port = "*", 19090
clients = {}			-- 所有client的列表
tasks = {}          -- 所有task的列表







function mainLoop ()
	clientCount = 0
	print("server: create socket ...")
	
	server = assert(socket.bind(host, port))
	
	print("server: waiting for client connection ...")
	
	while true do
		local client = assert(server:accept())
		
		if not (client == nil) then 
			local task = coroutine.wrap(function(sok)
				receive(sok)
				end)
			clientCount = clientCount + 1
			print("new client connected, total count:" .. clientCount)
			--计算table 大小
			clearTable()  --清理数组
			size = getTaskSize()
			if size > 10 then
				table.remove(clients,1)  --超大，减去第一个
				table.remove(tasks,1)
			end
			table.insert(clients,client)
			table.insert(tasks,task)
			task(client)
			size = getTaskSize()
			print("table size:" .. size)
		end
	end
end

function receive (sok)
	print("start rev Thread ...")
	while true do
		command, status = sok:receive()

		if status == "closed" then
			print("sock close, remove client from table")
			local i = 1
			while true do
				if clients[i] == sok then
					table.remove(clients,i)   --remove closed socket from table
					break
				end
				i = i+1
			end
		end
		print("received command:" .. command)
		if not (string.find(command, "WCAR", 1) == null) then
			--说明是合法cmd
			local i = 1
			
			while true do
				if not (clients[i] == nil) then
					if not (clients[i] == client) then
						client:send(command) 
						print("find a client, send cmd:" .. command)
					end
				else
					break
				end
				i = i + 1
			end
		end
	end
end

function getTaskSize ()
	local i = 1
	while true do
		if tasks[i] == nil then
			return i-1
		end
		i = i + 1
	end
end

function clearTable ()
	local i = 1
	while true do
		if not (tasks[i] == nil) then
			local res = tasks[i]()
			if not res then
				table.remove(tasks,i)
				table.remove(clients,i)
			end
		else 
			break
		end
		i = i + 1
	end
end


mainLoop()
