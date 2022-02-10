import tensorflow as tf
import numpy
import datetime as datetime
import timeit
a = tf.Variable(1, name = "a")
b = tf.Variable(2, name = "b")
f = a + b
# tf.print(f) = 3


def maths(x,y,b):
    x = tf.matmul(x,y)
    x = x + b
    return x

# Below shows how we can use tf.function to apply a python function to tensors
graph = tf.function(maths)
x1 = tf.constant([[1,2]])# shape 1,2
y1 = tf.constant([[2],[3]])# shape 2,1
b1 = tf.constant(4)# no shape.
# Need the shape differences for matrix multiplication
tf_values = graph(x1,y1,b1).numpy()
print(tf_values)
