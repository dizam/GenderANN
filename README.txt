Java program that implements an artifical neural network that classifies images as Male or Female.

Our team name is a.out, and we submitted our code as a_dot_out.java. The following command runs our code, and we explicitly assume the first training directory is the male data whereas the second training directory is the female data:

java a_dot_out -train Male Female -test Test

-	The artificial neural network we chose is a simple 3 layer feed forward neural network that uses SIGMOID, and is trained using stochastic gradient descent backpropagation. We chose this network because it is a simple architecture to implement and could be trained to high accuracy. We used one 128 * 100 input nodes because there are 128 * 100 pixels per image. We used 8 hidden nodes because this seemed to give us the best results; more hidden nodes would lead to overfitting, and less hidden nodes gave us less accuracy. Finally, we used one output node since there is only one output per image: a number that can be thresholded to determine if it classified the image as male or female. Since all the inputs went into each hidden node and all the hidden nodes went into the output node, the neural network was fully connected. We did add a bias term for the input to the hidden node and output from the hidden node to ensure it could shift the decision boundary vertically if needed.

-	Moreover, we used a learning rate of 0.22 for changing the weights and used 25 epochs for training, since we found these to be optimal values for reaching high accuracy without overfitting through trial and error.
 
The program contains methods for cross validation, and prints the output on the test data as Male or Female with a confidence value.

