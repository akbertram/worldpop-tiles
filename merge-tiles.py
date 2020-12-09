import glob
import os
from shutil import copyfile
from PIL import Image 

zoom = 9
year = 2020
size = 2 ** zoom

for x in range(0, size):
  tileDir = str(year) + '/' + str(zoom) + '/' + str(x)
  os.makedirs('merged/' + tileDir, exist_ok=True)

  for y in range(0, size):
    tileImage = tileDir + '/' + str(y) + '.png'
    tileImages = glob.glob('country/*/'  + tileImage)
    if len(tileImages) == 1:
      copyfile(tileImages[0], 'merged/' + tileImage)
    elif len(tileImages) > 1:
      im = Image.open(tileImages[0])
      for i in range(1, len(tileImages)):
        imi = Image.open(tileImages[i])
        im.paste(imi, (0, 0), imi)
      im.save( 'merged/' + tileImage, format="png")  




  
