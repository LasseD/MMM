from pybricks.pupdevices import Motor, ColorDistanceSensor
from pybricks.parameters import Color, Port
from pybricks.tools import wait

track = Motor(Port.D)
track.run(140)
tower = Motor(Port.A)
tower.control.limits(200, 90)
sensor = ColorDistanceSensor(Port.B)

while True:
    sensor.light.on(Color.GREEN)
    while sensor.distance() > 80:
        wait(100)
    track.stop()
    sensor.light.on(Color.RED)
    wait(300)
    track.run(140)
    tower.run_angle(-250, 2000)
