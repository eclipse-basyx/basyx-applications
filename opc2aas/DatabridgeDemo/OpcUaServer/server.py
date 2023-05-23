from opcua import ua, Server
import random
import time

# Create a server instance
server = Server()

# Register a namespace URI
uri = "urn:example:opcua:server"
idx = server.register_namespace(uri)

# Create a new object node
root = server.get_objects_node()

# Create multiple layers of nodes
layer1 = root.add_object(idx, "Robot")
layer2 = layer1.add_object(idx, "UR3e")
layer3 = layer2.add_object(idx, "Data")

# Create a second layer on Level 1
layer1_second = root.add_object(idx, "Motor")
layer2_second = layer1_second.add_object(idx, "Lexium BMH")
layer3_second = layer2_second.add_object(idx, "Data")

# Create nodes with different datatypes in Layer3
layer3_string = layer3.add_variable(idx, "Manufacturer", "")
layer3_boolean = layer3.add_variable(idx, "InUse", False)
layer3_integer = layer3.add_variable(idx, "CurrentAngle", 0)
layer3_float = layer3.add_variable(idx, "BaseTemperature", 0.0)

# Create nodes with different datatypes in Layer3_Second
layer3_second_string = layer3_second.add_variable(idx, "Manufacturer", "")
layer3_second_boolean = layer3_second.add_variable(idx, "InUse", False)
layer3_second_integer = layer3_second.add_variable(idx, "RotationSpeed", 0)
layer3_second_float = layer3_second.add_variable(idx, "MotorCurrent", 0.0)

# Start the server
server.start()

try:
    while True:
        # Update values for nodes in Layer3 with different datatypes
        layer3_string.set_value("Universal Robots")
        layer3_boolean.set_value(bool(random.getrandbits(1)))
        layer3_integer.set_value(random.randint(0, 100))
        layer3_float.set_value(random.uniform(20.0, 21.0))

        # Update values for nodes in Layer3_Second with different datatypes
        layer3_second_string.set_value("Schneider Electric")
        layer3_second_boolean.set_value(bool(random.getrandbits(1)))
        layer3_second_integer.set_value(random.randint(0, 100))
        layer3_second_float.set_value(random.uniform(0.5, 1.0))

        # Print the current values
        print("Layer3 String:", layer3_string.get_value())
        print("Layer3 Boolean:", layer3_boolean.get_value())
        print("Layer3 Integer:", layer3_integer.get_value())
        print("Layer3 Float:", layer3_float.get_value())
        print("Layer3_Second String:", layer3_second_string.get_value())
        print("Layer3_Second Boolean:", layer3_second_boolean.get_value())
        print("Layer3_Second Integer:", layer3_second_integer.get_value())
        print("Layer3_Second Float:", layer3_second_float.get_value())

        # Wait for some time before updating the values again
        time.sleep(1)

finally:
    # Stop the server
    server.stop()
