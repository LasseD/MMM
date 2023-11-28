from pybricks.pupdevices import Motor, ColorDistanceSensor, Light
from pybricks.parameters import Color, Port
from pybricks.tools import wait

track = Motor(Port.B)
track.run(140)
waitress = Motor(Port.D)
waitress.reset_angle()
sensor = ColorDistanceSensor(Port.C)
light = Light(Port.A)
angle = waitress.angle()

def present():
    global angle
    angle = angle + 120
    light.off()
    waitress.run_target(400, angle)
    light.on(100)
    wait(750)

while True:
    sensor.light.on(Color.GREEN)
    while sensor.distance() > 80:
        wait(100)
    track.stop()
    sensor.light.on(Color.RED)
    wait(500)
    track.run(140)
    present()
    present()
    present()
