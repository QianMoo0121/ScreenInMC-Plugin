int* intToRgba(int rgb) {\nint result[4];\nresult[0] = (rgb >> 16) & 0xff;\nresult[1] = (rgb >>  8) & 0xff;\nresult[2] = (rgb  ) & 0xff;\nresult[3] = (rgb >> 24) & 0xff;\nreturn result;\n}\nint rgbToInt(int r,int g,int b) {\nif(r>255) {\nr=255;\n}\nif(g>255) {\ng=255;\n}\nif(b>255) {\nb=255;\n}\nif(r<0) {\nr=0;\n}\nif(g<0) {\ng=0;\n}\nif(b<0) {\nb=0;\n}\nreturn 0xFF000000 | ((r << 16) & 0x00FF0000) | ((g << 8) & 0x0000FF00) | (b & 0x000000FF);\n}\nint colorDistance(int* c1, int* c2) {\nint rmean = (c1[0] + c2[0]) / 2;\nint r = c1[0] - c2[0];\nint g = c1[1] - c2[1];\nint b = c1[2] - c2[2];\nreturn (int)(sqrt((float)((((512+rmean)*r*r)>>8) + 4*g*g + (((767-rmean)*b*b)>>8))));\n}\nint* getNearlyColor(__global int *palette,int colorCount,int* rgb) {\nint* minColor= intToRgba(palette[0]);\nint min = colorDistance(minColor,rgb);\nint minIndex = 0;\nfor (int i=1;i<colorCount;i++) {\nint* tempColor = intToRgba(palette[i]);\nint temp = colorDistance(tempColor,rgb);\nif(temp<min) {\nmin = temp;\nminIndex = i;\nminColor[0]=tempColor[0];\nminColor[1]=tempColor[1];\nminColor[2]=tempColor[2];\n}\n}\nint result[5];\nresult[0]=minIndex+4;\nresult[1]=palette[minIndex];\nresult[2]=rgb[0]-minColor[0];\nresult[3]=rgb[1]-minColor[1];\nresult[4]=rgb[2]-minColor[2];\nreturn result;\n}\n__kernel void dither(__global int *colors,__global int *palette,__global int *settings,__global char *result) {\nint id = get_global_id(0);\nint width = settings[0];\nint pieceSize=settings[2];\nint r = id*pieceSize/width*width/pieceSize*pieceSize*pieceSize+id%(width/pieceSize)*pieceSize;\nint colorCount = settings[1];\nfor (int y = 0; y < pieceSize; ++y) {\nfor (int x = 0; x < pieceSize; ++x) {\nint* rgba = intToRgba(colors[r]);\nif(rgba[3]==255) {\nint* near = getNearlyColor(palette,colorCount,rgba);\ncolors[r] = near[1];\nresult[r] = (char)((near[0] / 4) << 2 | (near[0] % 4) & 3);\nif(x != pieceSize-1) {\nint index_ = r+1;\nint* rgba_ = intToRgba(colors[index_]);\nif(rgba_[3]==255) {\ncolors[index_]=rgbToInt(rgba_[0]+near[2]*0.4375,rgba_[1]+near[3]*0.4375,rgba_[2]+near[4]*0.4375);\n}\nif(y != pieceSize-1) {\nindex_ += width;\nrgba_ = intToRgba(colors[index_]);\nif(rgba_[3]==255) {\ncolors[index_]=rgbToInt(rgba_[0]+near[2]*0.0625,rgba_[1]+near[3]*0.0625,rgba_[2]+near[4]*0.0625);\n}\n}\n}\nif(y != pieceSize-1) {\nint index_ = r+width;\nint* rgba_ = intToRgba(colors[index_]);\nif(rgba_[3]==255) {\ncolors[index_]=rgbToInt(rgba_[0]+near[2]*0.1875,rgba_[1]+near[3]*0.1875,rgba_[2]+near[4]*0.1875);\n}\nif(x != 0) {\nindex_ -= 1;\nrgba_ = intToRgba(colors[index_]);\nif(rgba_[3]==255) {\ncolors[index_]=rgbToInt(rgba_[0]+near[2]*0.3125,rgba_[1]+near[3]*0.3125,rgba_[2]+near[4]*0.3125);\n}\n}\n}\n}else{\nresult[r]=0;\n}\nr++;\n}\nr+=width-pieceSize;\n}\n}