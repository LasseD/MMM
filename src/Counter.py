from pybricks.hubs import InventorHub
from pybricks.pupdevices import Motor, ColorDistanceSensor
from pybricks.parameters import Button, Color, Direction, Port, Side, Stop
from pybricks.tools import wait, StopWatch, Matrix
from umath import floor

hub = InventorHub()
hub.display.orientation(Side.RIGHT)
motor = Motor(Port.D)
sensor = ColorDistanceSensor(Port.F)
cnt = 0

def seesFigure():
    return sensor.distance() < 100

class Marquee:
    def __init__(self, hub):
        self.display = hub.display
        self.timer = StopWatch()
        self.DOTS = [
            ['AAA', 'BB ', ' CC', 'DD ', 'EEE', 'FFF', ' GG ', 'H H', 'III', 'JJJ', 'K  K', 'L  ', 'M   M', 'N   N', ' OO ', 'PPP ', ' QQ ', 'RRR ', 'SSS', 'TTT', 'U  U', 'V   V', 'W   W', 'X   X', 'Y   Y', 'ZZZZ', '000', ' 1 ', '222', '333', '4 4', '555', '666', '777', '888', '999', '!', '"" ""', ' # # ', ' $$$$', '%%  %', ' &&& ', "'", '\\  ', ' (', ') ', '   ', '   ', ' ', '  ', ' ', '  /', ' ', ' ', '  ', '  ', '  ', ' ??? ', ' @@@ ', '[[', ']]', ' ^ ', '   ', '` ', ' \{\{', '|', '\}  ' , ' '],
            ['A A', 'B B', 'C  ', 'D D', 'E  ', 'F  ', 'G   ', 'H H', ' I ', '  J', 'K K ', 'L  ', 'MM MM', 'NN  N', 'O  O', 'P  P', 'Q  Q', 'R  R', 'S  ', ' T ', 'U  U', 'V   V', 'W   W', ' X X ', ' Y Y ', '   Z', '0 0', '11 ', '  2', '  3', '4 4', '5  ', '6  ', '  7', '8 8', '9 9', '!', '     ', '#####', '$ $  ', '%% % ', '&&   ', "'", '\\  ', '( ', ' )', '* *', ' + ', ' ', '  ', ' ', '  /', ':', ';', ' <', '==', '> ', '?   ?', '@ @ @', '[ ', ' ]', '^ ^', '   ', ' `', ' \{ ' , '|', ' \} ' , ' '],
            ['AAA', 'BBB', 'C  ', 'D D', 'EE ', 'FF ', 'G GG', 'HHH', ' I ', '  J', 'KK  ', 'L  ', 'M M M', 'N N N', 'O  O', 'PPP ', 'Q  Q', 'RRR ', 'SSS', ' T ', 'U  U', 'V   V', 'W   W', '  X  ', '  Y  ', ' ZZ ', '0 0', ' 1 ', '222', '333', '444', '555', '666', '  7', '888', '999', '!', '     ', ' # # ', ' $$$ ', '  %  ', ' && &', " ", ' \\ ', '( ', ' )', ' * ', '+++', ' ', '--', ' ', ' / ', ' ', ' ', '< ', '  ', ' >', '   ? ', '@ @@@', '[ ', ' ]', '   ', '   ', '  ', '\{\{ ', '|', ' \}\}', ' '],
            ['A A', 'B B', 'C  ', 'D D', 'E  ', 'F  ', 'G  G', 'H H', ' I ', 'J J', 'K K ', 'L  ', 'M   M', 'N  NN', 'O  O', 'P   ', 'Q QQ', 'R R ', '  S', ' T ', 'U  U', ' V V ', 'W W W', ' X X ', '  Y  ', 'Z   ', '0 0', ' 1 ', '2  ', '  3', '  4', '  5', '6 6', '  7', '8 8', '  9', ' ', '     ', '#####', '  $ $', ' % %%', '&  &&', " ", '  \\', '( ', ' )', '* *', ' + ', ',', '  ', ' ', '/  ', ':', ';', ' <', '==', '> ', '     ', '@    ', '[ ', ' ]', '   ', '   ', '  ', ' \{ ' , '|', ' \} ' , ' '],
            ['A A', 'BB ', ' CC', 'DD ', 'EEE', 'F  ', ' GG ', 'H H', 'III', 'JJJ', 'K  K', 'LLL', 'M   M', 'N   N', ' OO ', 'P   ', ' Q Q', 'R  R', 'SSS', ' T ', ' UU ', '  V  ', ' W W ', 'X   X', '  Y  ', 'ZZZZ', '000', '111', '222', '333', '  4', '555', '666', '  7', '888', '  9', '!', '     ', ' # # ', '$$$$ ', '%  %%', ' && &', " ", '  \\', ' (', ') ', '   ', '   ', ',', '  ', '.', '/  ', ' ', ';', '  ', '  ', '  ', '  ?  ', ' @@@@', '[[', ']]', '   ', '___', '  ', ' \{\{', '|', '\}  ' , ' ']
        ]
        CHARS = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!"#$%&\'\\()*+,-./:;<=>?@[]^_`' + "{" + "|" + "} "
        SMALL = 'abcdefghijklmnopqrstuvwxyz'
        self.map = {}
        for i in range(0, len(CHARS)):
            c = CHARS[i]
            self.map[c] = i
        for i in range(0, len(SMALL)):
            self.map[SMALL[i]] = i
        self.write('')
    def write(self, text, lettersPerSecond = 0.8):
        self.lettersPerSecond = lettersPerSecond
        # Set up pixels:
        self.PIXELS = [[], [], [], [], []]
        for i in range(0, len(text)):
            c = text[i]
            dotIdx = self.map[c]
            for row in range(0, 5):
                s = self.DOTS[row][dotIdx]
                for j in range(0, len(s)):
                    self.PIXELS[row].append(s[j])
                self.PIXELS[row].append(' ')
    def update(self):
        LEN = len(self.PIXELS[0])
        x0 = self.timer.time()/1000 * (self.lettersPerSecond*6) # 6 is the average letter width + spacing
        x0Int = floor(x0)
        x0Diff = x0-x0Int
        x0 = x0Int % LEN
        for row in range(0, 5):
            for column in range(0, 5):
                percent0 = 100 if self.PIXELS[row][(x0+column)%LEN] != ' ' else 0
                self.display.pixel(row, column, percent0)

def pad2(x):
    if x < 10:
        return '0' + str(x)
    return str(x)

m = Marquee(hub)
#m.write('ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!"#$%&\'()*+,-./:;<=>?@[]^_`\{|\}')
motor.run(140)
occupied = seesFigure()
while True:
    # Update cnt:
    if seesFigure():
        if not occupied:
            hub.speaker.beep()
            occupied = True
            cnt = cnt + 1
            hub.light.on(Color.GREEN)
    else:
        if occupied:
            occupied = False
            hub.light.on(Color.BLUE)
    # Update display:
    t = floor(m.timer.time()/1000)
    SS = t % 60
    t = floor(t/60)
    MM = t % 60
    H = floor(t/60)
    s = pad2(MM) + ':' + pad2(SS)
    if H > 0:
        s = str(H) + ':' + s 
    m.write('  ' + s + '  ' + str(cnt))
    m.update()
    wait(20)
