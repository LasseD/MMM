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

colors = [Color.BLUE, Color.BLUE, Color.BLUE, 
          Color.RED, Color.YELLOW, Color.GREEN]
colorIdx = 0 # First get 3 normal people inside (gray reads as blue)

turnTarget = 0
def turn():
    global turnTarget
    turnTarget -= 600 # 600 = 360*5/3/3*3
    turner.run_target(TURN_SPEED, turnTarget)

def go():
    track.run(TRACK_SPEED)
    track2.run(36)

def goAngle(angle):
    track2.run(36)
    track.run_angle(TRACK_SPEED, angle)
    track2.stop()

def stop():
    track.stop()
    track2.stop()

# Change the figure. Assume the figure starts right over the color sensor.
def change():
    goAngle(220) # Get figure in front of entrance

    track.run(40) # Ensure figure transition to track 2
    track2.run_angle(TRACK2_SPEED, 1000) # Get figure inside

    track.stop() # Prevent second figure from getting inside
    track2.run_angle(TRACK2_SPEED, 2500) # Get figure fully inside

    track.run_angle(TRACK_SPEED, -200) # Make a bit more space outside
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
    while sinceLast < 40:
        sinceLast += 1
        wait(150)
        if sensor.color() != Color.NONE:
            sinceLast = 0
    print('Clean!')

print('Console debugging enabled!')
while True:
    wait(180)
    color = sensor.color()
    if color != Color.NONE:
        stop()
        goAngle(110) # Test again when center above sensor        
        wait(100) # Stop to read color properly
        color = sensor.color()
        print('Looking for color',colors[colorIdx],'found',color)
        if color is colors[colorIdx]:
            colorIdx += 1
            if colorIdx == 6:
                colorIdx = 3
            change()
        clearSensor()
