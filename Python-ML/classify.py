# SKLEARN IMPORTS
from sklearn.utils import shuffle
from sklearn.svm import SVC
from sklearn.linear_model import LogisticRegression
from sklearn.multiclass import OneVsRestClassifier
from sklearn.preprocessing import label_binarize
from sklearn.ensemble import RandomForestClassifier
from sklearn.neural_network import MLPClassifier
from sklearn.model_selection import train_test_split, GridSearchCV
from sklearn.preprocessing import StandardScaler
from sklearn.feature_selection import SelectPercentile
from sklearn.feature_selection import SelectFromModel
from sklearn.feature_selection import RFE

def run_classifiers(data, label):
    # Scale the data
    scaler = StandardScaler()
    data = scaler.fit_transform(data)

    # Split the dataset
    X_sensor_train, X_sensor_test, \
        y_sensor_train, y_sensor_test = train_test_split(data,
                                                         label,
                                                         random_state=1)
    # Set up the feature selected datasets
    # Recursive Feature Elmination
    select = RFE(RandomForestClassifier(n_estimators=100, random_state=42), n_features_to_select=40)
    select.fit(X_sensor_train, y_sensor_train)
    X_train_rfe = select.transform(X_sensor_train)
    X_test_rfe = select.transform(X_sensor_test)
    # Model based Feature Selection
    select = SelectFromModel(RandomForestClassifier(n_estimators=100, random_state=42), threshold="median")
    select.fit(X_sensor_train, y_sensor_train)
    X_train_model = select.transform(X_sensor_train)
    X_test_model = select.transform(X_sensor_test)
    # Univariate Model Selection
    select = SelectPercentile(percentile=50)
    select.fit(X_sensor_train, y_sensor_train)
    X_train_uni = select.transform(X_sensor_train)
    X_test_uni = select.transform(X_sensor_test)

    # SVC
    param_grid = [
        {'C': [0.001, 0.01, 0.1, 1, 10, 100], 'kernel': ['linear']},
        {'C': [0.001, 0.01, 0.1, 1, 10, 100],
         'gamma': [1, 0.1, 0.01, 0.001],
         'kernel': ['rbf']}, ]
    grid = GridSearchCV(SVC(), param_grid=param_grid, cv=10)
    # No selection
    grid.fit(X_sensor_train, y_sensor_train)
    print('Best score for SVC: {}'.format(grid.score(X_sensor_test, y_sensor_test)))
    print('Best parameters for SVC: {}'.format(grid.best_params_))
    # RFE
    grid.fit(X_train_rfe, y_sensor_train)
    print('Best score for RFE SVC: {}'.format(grid.score(X_test_rfe, y_sensor_test)))
    print('Best parameters for SVC: {}'.format(grid.best_params_))
    # Model based
    grid.fit(X_train_model, y_sensor_train)
    print('Best score for Model SVC: {}'.format(grid.score(X_test_model, y_sensor_test)))
    print('Best parameters for SVC: {}'.format(grid.best_params_))
    # Univariate
    grid.fit(X_train_uni, y_sensor_train)
    print('Best score for Univariate SVC: {}'.format(grid.score(X_test_uni, y_sensor_test)))
    print('Best parameters for SVC: {}'.format(grid.best_params_))

    # Linear Regression
    param_grid = {'C': [0.001, 0.01, 0.1, 1, 10]}
    grid = GridSearchCV(LogisticRegression(), param_grid=param_grid, cv=10)
    # No selection
    grid.fit(X_sensor_train, y_sensor_train)
    print('Best score for Logistic Regression: {}'.format(grid.score(X_sensor_test, y_sensor_test)))
    print('Best parameters for Logistic Regression: {}'.format(grid.best_params_))
    # RFE
    grid.fit(X_train_rfe, y_sensor_train)
    print('Best score for RFE Logistic Regression: {}'.format(grid.score(X_test_rfe, y_sensor_test)))
    print('Best parameters for Logistic Regression: {}'.format(grid.best_params_))
    # Model based
    grid.fit(X_train_model, y_sensor_train)
    print('Best score for Model Logistic Regression: {}'.format(grid.score(X_test_model, y_sensor_test)))
    print('Best parameters for Logistic Regression: {}'.format(grid.best_params_))
    # Univariate
    grid.fit(X_train_uni, y_sensor_train)
    print('Best score for Univariate Logistic Regression: {}'.format(grid.score(X_test_uni, y_sensor_test)))
    print('Best parameters for Logistic Regression: {}'.format(grid.best_params_))

    # Random Forest
    rf = RandomForestClassifier(n_estimators=200)
    param_grid = {'max_features': ['sqrt', 'log2', 10],
                  'max_depth': [5, 7, 9]}
    grid = GridSearchCV(rf, param_grid, cv=10)
    # No selection
    grid.fit(X_sensor_train, y_sensor_train)
    print('Best score for Random Forest: {}'.format(grid.score(X_sensor_test, y_sensor_test)))
    print('Best parameters for Random Forest: {}'.format(grid.best_params_))
    # RFE
    grid.fit(X_train_rfe, y_sensor_train)
    print('Best score for RFE Random Forest: {}'.format(grid.score(X_test_rfe, y_sensor_test)))
    print('Best parameters for Random Forest: {}'.format(grid.best_params_))
    # Model based
    grid.fit(X_train_model, y_sensor_train)
    print('Best score for Model Random Forest: {}'.format(grid.score(X_test_model, y_sensor_test)))
    print('Best parameters for Random Forest: {}'.format(grid.best_params_))
    # Univariate
    grid.fit(X_train_uni, y_sensor_train)
    print('Best score for Univariate Random Forest: {}'.format(grid.score(X_test_uni, y_sensor_test)))
    print('Best parameters for Random Forest: {}'.format(grid.best_params_))


    # MLP (Nerual Network)
    params = [{'solver': ['sgd'], 'learning_rate': ['constant'], 'momentum': [0],
               'learning_rate_init': [0.2]},
              {'solver': ['sgd'], 'learning_rate': ['constant'], 'momentum': [.9],
               'nesterovs_momentum': [False], 'learning_rate_init': [0.2]},
              {'solver': ['sgd'], 'learning_rate': ['constant'], 'momentum': [.9],
               'nesterovs_momentum': [True], 'learning_rate_init': [0.2]},
              {'solver': ['sgd'], 'learning_rate': ['invscaling'], 'momentum': [0],
               'learning_rate_init': [0.2]},
              {'solver': ['sgd'], 'learning_rate': ['invscaling'], 'momentum': [.9],
               'nesterovs_momentum': [True], 'learning_rate_init': [0.2]},
              {'solver': ['sgd'], 'learning_rate': ['invscaling'], 'momentum': [.9],
               'nesterovs_momentum': [False], 'learning_rate_init': [0.2]},
              {'solver': ['adam'], 'learning_rate_init': [0.01]}]

    grid = GridSearchCV(MLPClassifier(), param_grid=params, cv=3)
    y = label_binarize(y_sensor_test, classes=[0, 1, 2])
    grid.fit(X_sensor_train, y_sensor_train)
    print('Best score for MLP: {}'.format(grid.score(X_sensor_test, y_sensor_test)))
    print('Best parameters for MLP: {}'.format(grid.best_params_))
