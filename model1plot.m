% Create scatter of coordinates and read points of interest
coordinates = dlmread('Rottermanhattan_coordinates.txt');
ambulances = dlmread('ambulances.txt');
helicopters = dlmread('helicopters.txt');
alphaobj = dlmread('objectivesJerry.txt');
sizeA=size(ambulances);
sizeH=size(helicopters);
ambulancesCoord.x=ambulances(:,1);
ambulancesCoord.y=ambulances(:,2);
helicoptersCoord.x=helicopters(:,1);
helicoptersCoord.y=helicopters(:,2);
s = scatter(coordinates(:,1),coordinates(:,2),'Marker','.');

% Add xlabel, ylabel, title, and legend
xlabel('x')
ylabel('y')
title('Scatter of Points of Interest')
legend

set(legend(s),'visible','off')

% plot ambulance points
hold on 
scatter(ambulancesCoord.x, ambulancesCoord.y, 'black');

% create diamonds to show coverage
hold on
for i = 1:5
x1=ambulancesCoord.x(i,1) - 18.75;
x2=ambulancesCoord.x(i,1) + 18.75;
x3=ambulancesCoord.x(i,1);
y1=ambulancesCoord.y(i,1) + 18.75;
y2=ambulancesCoord.y(i,1) - 18.75;
y3=ambulancesCoord.y(i,1);

x = [x1, x3, x2, x3, x1];
y = [y3, y1, y3, y2, y3];
plot(x, y, 'b-', 'LineWidth', 0.5);
hold on;

end

% plot helicopter points
hold on 
scatter(helicoptersCoord.x,helicoptersCoord.y, 'black');

% create circles to show coverage
hold on 
for i = 1:2
hold on
r = 33.3333;
th = 0:pi/50:2*pi;
xunit = r * cos(th) + helicoptersCoord.x(i,1);
yunit = r * sin(th) + helicoptersCoord.y(i,1);
plot(xunit, yunit);
hold off

end
