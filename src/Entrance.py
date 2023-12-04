from pybricks.pupdevices import Motor, ColorDistanceSensor, InfraredSensor
from pybricks.parameters import Color, Port
from pybricks.tools import wait, StopWatch

SPEED_TRACK_FAST = 140
SPEED_TRACK_SLOW = 60
TIME_WAIT_FIGURE = 2500

innerTrack = Motor(Port.D)
outerTrack = Motor(Port.B)

figureSensor = InfraredSensor(Port.C)
activationSensor = ColorDistanceSensor(Port.A)
watch = StopWatch()
state = 0 
# 0: Figure seen. Outer track runs. Wait until TIME_WAIT_FIGURE elapsed
# 1: Free for interaction
# 2: Interaction

while True:
    wait(100)
    if figureSensor.distance() < 26:
        watch.reset()
        if state != 0:
            state = 0
            activationSensor.light.on(Color.RED)
            innerTrack.stop()
            outerTrack.run(SPEED_TRACK_FAST)
    if watch.time() < TIME_WAIT_FIGURE:
        continue

    if activationSensor.distance() < 80:
        if state != 2:
            activationSensor.light.on(Color.GREEN)
            state = 2
            innerTrack.run(SPEED_TRACK_FAST)
            outerTrack.run(SPEED_TRACK_SLOW)
    else:
        if state != 1:
            activationSensor.light.on(Color.GREEN)
            state = 1
            innerTrack.stop()
            outerTrack.run(SPEED_TRACK_FAST)

