from pybricks.hubs import MoveHub
from pybricks.pupdevices import Motor, ColorDistanceSensor
from pybricks.parameters import Color, Port, Stop
from pybricks.tools import wait

hub = MoveHub()
sensor = ColorDistanceSensor(Port.D)
track = Motor(Port.C)
trackInside = Motor(Port.A)
turner = Motor(Port.B)
TURN_SPEED = 100
TRACK_SPEED = 130
TRACK_INSIDE_SPEED = 3*TRACK_SPEED # Due to 1:3 gearing

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
    trackInside.run_angle(TRACK_INSIDE_SPEED, 1200, Stop.COAST, False) # Help the turner
    turnTarget -= 600 # 600 = 360*5/3/3*3
    turner.run_target(TURN_SPEED, turnTarget)

def go():
    track.run(TRACK_SPEED)
    trackInside.run(35)

def goAngle(angle):
    trackInside.run(35)
    track.run_angle(TRACK_SPEED, angle)
    trackInside.stop()

def stop():
    track.stop()
    trackInside.stop()

def clearSensor():
    print('Clearing sensor')
    go()
    sinceLast = 0
    while sinceLast < 48:
        sinceLast += 1
        wait(150)
        if sensor.color() != Color.NONE:
            sinceLast = 0
    stop()
    print('Clean!')

# Change the figure. Assume the figure starts right over the color sensor.
def change():
    goAngle(220) # Get figure in front of entrance

    track.run(40) # Ensure figure transitions to track 2
    trackInside.run_angle(TRACK_INSIDE_SPEED, 1500) # Get figure inside

    clearSensor()
    trackInside.run_angle(TRACK_INSIDE_SPEED, 2000) # Get figure fully inside

    turn()

    # Get figure from inside out
    trackInside.run_angle(TRACK_INSIDE_SPEED, -1500)
    track.run(30)
    trackInside.run_angle(TRACK_INSIDE_SPEED, -2000)
    go()

print()
print('Console debugging enabled!')
while True:
    wait(180)
    go()
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
