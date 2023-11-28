def S(n):
    return 1 << n


def act_sys(pt1=False):
    seen_states = set()
    last_state = None

    r0 = r1 = r2 = r3 = r4 = r5 = 0
    while True:
        r1 = r5 | S(16)
        r5 = 4591209
        while True:
            # r3 = r1 % (1 << 8)
            # r5 = r5 + r3
            # r5 = r5 % (1 << 24)
            # r5 = r5 * 65899
            # r5 = r5 % (1 << 24)
            r5 = ((r5 + (r1 % S(8)) % S(24)) * 65899) % S(24)
            if r1 < S(8):
                # pt 1.
                if pt1:
                    return r5

                # return
                if r5 == r0:
                    return r0
                else:
                    curr_state = (r5,)
                    if curr_state in seen_states:
                        return last_state[-1]
                    seen_states.add(curr_state)
                    last_state = curr_state
                    break
            # r3 = 0
            # while True:
            #     if ((r3 + 1) << 8) > r1:
            #         r1 = r3
            #         break
            #     r3 += 1
            r1 = r1 // (1 << 8)


print(act_sys(pt1=True))
print(act_sys(pt1=False))
