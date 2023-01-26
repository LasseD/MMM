from pybricks.hubs import InventorHub
from pybricks.pupdevices import Motor, ColorSensor, UltrasonicSensor, ColorLightMatrix
from pybricks.parameters import Button, Color, Port, Stop
from pybricks.tools import wait

# State:
hub = InventorHub()
disableShooting = True
track = Motor(Port.F)
shooter = Motor(Port.B)
horses = Motor(Port.D)
colorSensor = ColorSensor(Port.E)
colorSensor.detectable_colors([Color.RED, Color.YELLOW, Color.CYAN, Color.NONE])
color = Color.NONE # Last read color
indicator = ColorLightMatrix(Port.A)
distanceSensor = UltrasonicSensor(Port.C)
dPrevPrev = dPrev = distanceSensor.distance()
state = -1
hub.display.off() # Reduce power usage

def reset():
    horses.run_until_stalled(-200, Stop.COAST, 25)
    horses.run_angle(200, 300)

# Magical constants corresponding to having built the model exactly as in the
# instructions with the motor in the default position.
def shoot():
    if shooter.angle() > 100:
        shooter.track_target(-147)
    else:
        shooter.track_target(164)
    horses.run_angle(500, 80, Stop.COAST_SMART, True)
    shooter.stop()

def updateColor():
    global color
    color = colorSensor.color()
    # Show current color in main button
    hub.light.on(color) 

def trigger():
    global dPrev, dPrevPrev, state
    d = distanceSensor.distance()
    if d == 2000:
        state = -1
        return False # No value read
    if d == dPrev:
        return False # No change
    print(d)
    state = 0
    if d < dPrev:
        state = state + 1
    if dPrev < dPrevPrev:
        state = state + 1
    dPrevPrev = dPrev
    dPrev = d
    return state == 2 or d < 300

def updateLights():
    if state == -1: # No read
        indicator.on([Color.RED, Color.NONE, Color.RED,
                      Color.NONE, Color.RED, Color.NONE,
                      Color.RED, Color.NONE, Color.RED])
    elif state == 2:
        indicator.on([Color.RED, Color.YELLOW, Color.CYAN,
                      Color.RED, Color.YELLOW, Color.CYAN,
                      Color.RED, Color.YELLOW, Color.CYAN])
    elif state == 1:
        indicator.on([Color.RED, Color.YELLOW, Color.CYAN,
                      Color.RED, Color.YELLOW, Color.CYAN,
                      Color.NONE, Color.NONE, Color.NONE])
    else:
        indicator.on([Color.RED, Color.YELLOW, Color.CYAN,
                      Color.NONE, Color.NONE, Color.NONE,
                      Color.NONE, Color.NONE, Color.NONE])

track.run(-140)
reset()

# Main loop (wait 200ms for sensor to read): When closer x 2: shoot
# Reset horses when color is seen.
while not hub.buttons.pressed():
    wait(200)
    if trigger():
        updateLights()
        shoot()
    updateLights()
    updateColor()
    if color != Color.NONE:
        indicator.on(color) # Indicate winning horse
        reset()

indicator.off()
track.stop()
SystemExit()
