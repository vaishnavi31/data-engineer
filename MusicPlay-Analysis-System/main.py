from create_tables import main as mainForDataBaseSchema
from etl import main as mainForDataProcessing

if __name__ == "__main__":
    mainForDataBaseSchema()
    mainForDataProcessing()