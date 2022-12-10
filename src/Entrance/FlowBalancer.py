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

EXPECTED_GAP = 8000
last = utime.ticks_ms()-EXPECTED_GAP

while True:
    p1 = track.get_degrees_counted()
    wait_for_seconds(0.1)
    if abs(track.get_degrees_counted()-p1) < 2:
        track.stop()
        track.run_for_degrees(150)
        track.run_for_degrees(-150)
        track.run_for_degrees(300)
        track.start()

    if last + EXPECTED_GAP*2 < utime.ticks_ms():
        inside.run_for_seconds(1.3) # Add to track
        last = utime.ticks_ms() # Reset

    c = color.get_color()    
    if not (c == 'black' or c == None):
        wait_for_seconds(1.5)
        if utime.ticks_ms() < last + EXPECTED_GAP*0.6: # Too crowded:
            inside.run_for_degrees(650)
        else: # All OK
            inside.run_for_degrees(25)
            last = utime.ticks_ms() # Reset
