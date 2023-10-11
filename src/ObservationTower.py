from pybricks.pupdevices import Motor, ColorDistanceSensor
from pybricks.parameters import Color, Port
from pybricks.tools import wait

Motor(Port.D).run(140) # Track
tower = Motor(Port.A)
sensor = ColorDistanceSensor(Port.B)

while True:
    sensor.light.on(Color.RED)
    while sensor.distance() == 10:
        wait(200)
    sensor.light.off()
    tower.run_angle(-200, 2400)
    
