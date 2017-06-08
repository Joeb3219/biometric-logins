# Biometric Login System

This is a Java prototype for a login system which uses a username and password, in addition to biometric data (*how* you input your password), to authenticate that you are the owner of the account.

## What are keystroke biometrics
Typically, individuals have a specific typing style, with factors such as:
* Key press length
* Duration between key presses
* When a key B is pressed after a key A, but without yet releasing key A.
* Caps lock vs shift

Over time, a user's typing style tends to be fairly consistent. Thus, it can be a reliable measure of the user's identity. 

This is far from a novel idea in terms of user verification in communication mediums:

> Using a methodology called "The Fist of the Sender," Military Intelligence identified that an individual had a unique way of keying in a message's "dots" and "dashes," creating a rhythm that could help distinguish ally from enemy. (https://en.wikipedia.org/wiki/Keystroke_dynamics)

## How the system works
The implemented system is a dummy system, IE: it does nothing more than allow registrations, and then can check if the supplied information is within valid ranges for a user. For practical uses, this system will need to be extended.

### Registration
Registration consists of 10 (though adjustable via `_NUM_TRAINING_SESSIONS` in `Register.java`) "verification rounds", in which the user must continually reenter their password.

During each password entry, each character and the time at which the key was pressed and released (microtime, relative to the Unix epoch) in stored. Upon submitting the password, the entry is "normalized", ie: we subtract the starting time from each key's time values (such that they are all zero based). 

Given all `_NUM_TRAINING_SESSIONS` of our entries, we can then calculate an average and standard deviation among the entries.

We also keep note of whether each key was typed with shift, caps lock, both, or neither.

These values are stored in a CSV file, `users.txt`, in the following format:
* username
* password
* for each character in the password: `start press time`, `end press time`, `start standard deviation`, `end stardard deviation`, `capslock`, `shift`

Where `start press time` and `end press time` are the average time upon which the key was pressed and released, respectively, the following two entries are the standard deviations between all entries, respectively, and capslock and shift are `1` if caps lock or shift were engaged, respectively.

### Login
Login is fairly similar to registration in terms of listening to keystrokes. We record which keys are pressed and when the key begins its press and ends its press.

We then compare if each key is within a weighted multiple of the standard deviation of the actual key. This can be adjusted via modification of `_STD_DEV_WEIGHT` in `User.java`. Typically, the larger this value, the less precise the system is. Smaller values allow very little irregularities in typing, though.
