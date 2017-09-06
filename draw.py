import numpy as np
import matplotlib.pyplot as plt
import math

x = []
y = [] # 10, not 9, so the fit isn't perfect

input_file = open("results/ff.txt")

i = 0
for line in input_file:
    if i > 0 and int(line) > 0:
        x.append(math.log(i))
        y.append(math.log(int(line)))
    if i >= 200:
        break
    i += 1

fit = np.polyfit(x,y,1)
fit_fn = np.poly1d(fit) 
# fit_fn is now a function which takes in x and returns an estimate for y

plt.plot(x,y, 'yo', x, fit_fn(x), '--k')
plt.show()
