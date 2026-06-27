CREATE TABLE db_example.employee (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    department VARCHAR(255),
    PRIMARY KEY (id)
);

INSERT INTO db_example.employee (name, department)
VALUES
('John Doe', 'IT'),
('Alice Smith', 'HR'),
('Bob Johnson', 'Finance');