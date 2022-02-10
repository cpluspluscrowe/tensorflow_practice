import tensorflow as tf

a = tf.Variable(1, name = "ab")
b = tf.Variable(2, name = "b")
f = a + b
tf.print(f)
