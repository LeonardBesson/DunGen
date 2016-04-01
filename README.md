# DunGen

A Java implementation of https://www.reddit.com/r/gamedev/comments/1dlwc4/procedural_dungeon_generation_algorithm_explained/ using LibGDX

![ScreenShot](http://i.imgur.com/iXoSRFY.png)

Sample of possible results : http://imgur.com/a/ZizIj

Customizable parameters :
  - Number of rooms & cells
  - Normal Distribution :
    - Standard deviation
    - Mean
    - Threshold used deferenciate rooms from cells (For example, if you want that your rooms get generated from the 90th percentile values of the normal distribution, choose N(0, 1) with a threshold of 1.65)
  - Size of rooms & cells
  - Orientation of rooms & cells
  - Width/height ratio of rooms & cells
  - Multiplier to scale normal distribution values
  - Rooms & cells gap
  - Percentage of re-added edges from the triangulation to the minimal spanning tree (If your MST contains 10 edges, with a percentage of 0.2, 2 edges from the initial triangulation will be added to the MST)
  - Size of corridors
  - Colors of debug
