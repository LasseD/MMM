import hub as primitiveHub
from spike import PrimeHub, LightMatrix, Button, StatusLight, Speaker, ColorSensor, App, DistanceSensor, Motor
from spike.control import wait_for_seconds, wait_until, Timer
from math import *
import utime

hub = PrimeHub()
track = Motor('B')
track.set_default_speed(-19)
track.start()
color = ColorSensor('A')
inside = Motor('D')
inside.set_default_speed(45)
dist = primitiveHub.port.C.device
d0 = 200
ticks = 0
seesMinifig = False
seen = 0
running = False

q = [9, 0, 0, 0]
def updateLights():
    if running:
        x = int(utime.ticks_ms()/200)%4
        dist.mode(5, bytes([q[(x+1)%4],q[(x+2)%4],q[(x+4)%4],q[(x+3)%4]]))
    else:
        x = 4 + int(4*cos(utime.ticks_ms()/400))
        dist.mode(5, bytes([x,x,x,x]))

ohno = 0
while True:
    updateLights()
    p1 = track.get_degrees_counted()
    wait_for_seconds(0.10)
    if abs(track.get_degrees_counted()-p1) < 1: # Maybe stuck!:
        ohno = ohno+1
        if(ohno > 10): # Get unstuck:
            track.stop()
            track.run_for_degrees(150)
            track.run_for_degrees(-150)
            track.run_for_degrees(300)
            track.start()
    else:
        ohno = 0

    d1 = dist.get()[0]
    d1 = 200 if d1 is None else d1
    #print(d1)
    
    if d1 < 200 and (d1 > d0 or (running and d1 == d0)):
        if not running:
            inside.start()
        running = True
    else:
        inside.stop()
        running = False
    d0 = d1

    c = color.get_color()
    if not (c == 'black' or c == None):
        if not seesMinifig:
            seen = seen+1
            #hub.speaker.beep(44+, 0.5) // Allowed 44->123
        seesMinifig = True
    else:
        seesMinifig = False
