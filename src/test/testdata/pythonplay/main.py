print("dd")
    
def bo():
    print("sda")


class Game:
    def __init__(self):
        print("I am here")
        self.player_count = 0
        self.debug_mode = False

    def start(self, player_count: int, **debug_mode):
        print("")
        self.player_count = player_count
        self.debug_mode = debug_mode
