
class AuthLogResolver(object):
    def __init__(self):
        self.buf = []
        self.division_count = 0
        self.got_space = False

    def assoiate_type(self):
        return 'auth'
    
    def handle(self, ch):
        if ch == ' ':
            if not self.got_space:
                self.division_count += 1
                self.got_space = True
        else:
            self.buf.append(ch)
            self.got_space = False

# (?P<timestamp>\w+ +\d+ +\d+:\d+:\d+) (?P<hostname>[^ ]+) +(?P<app>[^:]+):(?P<message>.*)
