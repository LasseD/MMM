from pybricks.hubs import MoveHub
from pybricks.pupdevices import Motor, ColorDistanceSensor
from pybricks.parameters import Color, Port, Stop
from pybricks.tools import wait

hub = MoveHub()
sensor = ColorDistanceSensor(Port.D)
track = Motor(Port.C)
track2 = Motor(Port.A)
turner = Motor(Port.B)
TURN_SPEED = 100
TRACK_SPEED = 130
TRACK2_SPEED = 3*TRACK_SPEED # Due to 1:3 gearing

acceptedColors = [Color.RED, Color.YELLOW, Color.WHITE]
colors = [Color.NONE, Color.NONE, Color.NONE]
colorIdx = -3

def acceptsColor(c):
    global colorIdx, colors, acceptedColors
    if colorIdx < 0: # Initially find 3 non-colored minifigs
        print('Initially find 3 non-colored figures')
        if c in acceptedColors:
            return False
        colorIdx += 1
        return True
    if colorIdx < 3: # Set up colors:
        if c in colors or not c in acceptedColors:
            return False
        colors[colorIdx] = c
        colorIdx += 1
        return True
    # Normal operation: Compare against saved colors:
    expectedColor = colors[colorIdx % 3]
    if c != expectedColor:
        print('Normal operation. Expected', expectedColor)
        return False
    colorIdx += 1
    return True

turnTarget = 0
def turn():
    global turnTarget
    turnTarget -= 600 # 600 = 360*5/3/3*3
    turner.run_target(TURN_SPEED, turnTarget)

def go():
    track.run(TRACK_SPEED)
    track2.run(35)

def goAngle(angle):
    track2.run(35)
    track.run_angle(TRACK_SPEED, angle)
    track2.stop()

def stop():
    track.stop()
    track2.stop()

# Change the figure. Assume the figure starts right over the color sensor.
def change():
    goAngle(220) # Get figure in front of entrance

    track.run(40) # Ensure figure transitions to track 2
    track2.run_angle(TRACK2_SPEED, 1500) # Get figure inside

    goAngle(3000) # Make other figs move past
    track.run_angle(TRACK_SPEED, -500) # Make space outside
    track2.run_angle(TRACK2_SPEED, 2000) # Get figure fully inside

    track2.run_angle(TRACK2_SPEED, 1200, Stop.COAST, False) # Help the turner
    turn()

    track2.run_angle(TRACK2_SPEED, -1500)
    track.run(30)
    track2.run_angle(TRACK2_SPEED, -2000)
    go()

go()

def clearSensor():
    print('Clearing sensor')
    go()
    sinceLast = 0
    while sinceLast < 30:
        sinceLast += 1
        wait(150)
        if sensor.color() != Color.NONE:
            sinceLast = 0
    print('Clean!')

print()
print('Console debugging enabled!')
while True:
    wait(180)
    color = sensor.color()
    if color != Color.NONE:
        stop()
        goAngle(110) # Test again when center above sensor        
        wait(200) # Stop to read color properly
        color = sensor.color()
        print('Found',color)
        if acceptsColor(color):
            change()
        clearSensor()
