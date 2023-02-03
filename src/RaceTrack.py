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
dPrev = distanceSensor.distance()

X = Color.NONE
R = Color.RED
G = Color.GREEN
B = Color.BLUE
Y = Color.YELLOW
STATE_LIGHTS = {
    -1: [X, X, X, X, R, X, X, X, X], # No read
    0: [X, B, X, B, B, B, X, B, X], # Default
    1: [X, B, X, B, B, B, Y, B, X], #
    2: [X, B, X, B, B, B, Y, B, Y], #
    3: [Y, B, Y, B, B, B, Y, B, X], #
    4: [Y, B, Y, B, B, B, Y, B, Y], #
    5: [X, G, X, G, G, G, X, G, X], # SHOOT!
}
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
    global dPrev, state

    d = distanceSensor.distance()

    if d == 2000:
        state = -1
        return False # No value read

    d = int(d/4) # Adjust for what feels good...
    if d >= dPrev:        
        dPrev = d
        state = 0
        return False # No change

    state = min((5 if state > -1 else 0), dPrev-d)
    print(dPrev,"->",d,"state",state)
    dPrev = d
    return state == 5

clearMid = False
def updateLights():
    global clearMid

    clearMid = not clearMid
    lights = STATE_LIGHTS[state]
    if state >= 0:
        if clearMid:
            lights[4] = X
        else:
            lights[4] = G if state == 5 else B 
    indicator.on(lights)

track.run(-140)
reset()

# Reset horses when color is seen.
while not hub.buttons.pressed():
    wait(250)
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
